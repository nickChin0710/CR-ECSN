/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 110/07/21  V1.00.01  Justin Wu   remove the action to change dir before get, put, rename, and delete    
* 110/07/29  V1.00.02  Justin Wu   assign SFTP session to the global variable*
* 111-01-18  V1.00.03  Justin Wu   fix Throw Inside Finally                  *
******************************************************************************/
package Dxc.Util.Ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;


public class SFtpClient extends FtpBase{

	private ChannelSftp command;
	private Session session;

	public SFtpClient() {
		command = null;
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


		//If the client is already connected, disconnect
		if (command != null) {
			disconnect();
		}
		
		JSch jsch=new JSch();
		try {
			session = jsch.getSession(loginId, host, port);
			session.setConfig("StrictHostKeyChecking", "no");
			
//			// Justin: 
//			// the following code can specify the HostKeyAlgorithms
//			// because in some scenario the SFTP server only accepts certain algorithms
//			if (AVON_IP.equals(sP_Host)) {	
//				session.setConfig("server_host_key", "ssh-dss");
//				StringBuffer sb = new StringBuffer();
//				try {
//					HostKeyRepository hkr = session.getHostKeyRepository();
//				    for(HostKey hk : hkr.getHostKey()){
//			            String type = hk.getType();
//			            sb.append(String.format("Host[%s],Type[%s]", hk.getHost(),type));
//				    }
//				}catch (Exception e) {
//					sb.append("SFTP Error:");
//					sb.append(e.getMessage());
//				}
//				setMesg(sb.toString());
//			}
			
			session.setPassword(loginKey);
			
			session.connect();
			
			Channel channel = session.openChannel("sftp");
			channel.connect();
			command = (ChannelSftp) channel;
			
		} catch (JSchException e1) {
			e1.printStackTrace();
			return false;
		}
		return command.isConnected();
		
		/*  Older Version SFTP Connect
		//If the client is already connected, disconnect
		if (command != null) {
			disconnect();
		}
		FileSystemOptions fso = new FileSystemOptions();
		try {
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fso, "no");
			session = SftpClientFactory.createConnection(host, port, loginId.toCharArray(), loginKey.toCharArray(), fso);
			Channel channel = session.openChannel("sftp");
			channel.connect();
			command = (ChannelSftp) channel;

			
		} catch (FileSystemException | JSchException e) {
			e.printStackTrace();
			return false;
		}
		return command.isConnected();
		*/
	}

	/**
	 * FTP Server 斷線
	 */
	public void disconnect()   {
		if (command != null) {
			command.exit();
		}
		if (session != null) {
			session.disconnect();
		}
		command = null;
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

	//public Vector<String> listFileInDir(String remoteDir) throws Exception {
	public Vector<String> listFileInDir()  {
		try {
//			Vector<LsEntry> rs = command.ls(remoteDir);
			Vector<LsEntry> rs = command.ls(".");
			Vector<String> result = new Vector<String>();
			for (int i = 0; i < rs.size(); i++) {
				if (!isARemoteDirectory(rs.get(i).getFilename())) {
					result.add(rs.get(i).getFilename());
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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

	public Vector<String> getSubDir(String remoteDir)  {
		
		Vector<String> result = new Vector<String>();
		try {
			Vector<LsEntry> rs = command.ls(remoteDir);
			for (int i = 0; i < rs.size(); i++) {
				if (isARemoteDirectory(rs.get(i).getFilename())) {
					result.add(rs.get(i).getFilename());
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			result=null;
		}
		return result;
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
			command.mkdir(remoteDirName);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	protected boolean downloadFileAfterCheck(String remotePath, String localPath) throws IOException {

		try (FileOutputStream outputSrr = new FileOutputStream(localPath);){
			File file = new File(localPath);
			if (!file.exists()) {
				
				command.get(remotePath, outputSrr);
			}
		} catch (SftpException e) {
			try {
				System.err.println(remotePath + " not found in " + command.pwd());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return false;
		} 
		return true;
	}

    /**
     * 刪除遠端檔案
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03          
     * @param remotePathFileName :遠端目錄名稱
     * @return 如果成功return true，否則return false
     * 
    */

	public boolean deleteRemoteFile(String remotePathFileName) throws IOException {

		boolean rst=true;

		try {
			command.rm(remotePathFileName);
		} catch (SftpException e) {
			rst = false;
		} finally {
		}
		return rst;
	}

    /**
     * 下載檔案
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
		
		try {
			try(FileOutputStream outputSrr = new FileOutputStream(localFileName);){
				command.get(remoteFileName, outputSrr);
				addFtpFileName(localFileName);
			}
		} catch (SftpException e) {
			try {
				System.err.println(remoteFileName + " not found in " + command.pwd());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	
    /**
        *上傳檔案
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03     
     * @param remotePath :遠端目錄名稱
     * @param localDir :本機目錄名稱
     * @param pattern ：檔案樣式
     *        sP_Pattern = "*txt"; // 上傳 檔名末三碼 = "txt" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg*"; // 上傳 檔名前三碼 = "msg" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg_07.txt"; // 上傳 檔名  = "msg_07.txt" 的檔案
 			  sP_Pattern = "_0?"; // 上傳 檔名含有 = "_0" 的檔案
			  sP_Pattern = "Msg*jar"; //上傳 檔名前三碼 = "Meg" and 檔名末三碼 = "jar" 的檔案 ( 有分大小寫 )

     * @return 如果成功return true，否則return false
     * 
    */

	public boolean mputFile(String remotePath, String localDir, String pattern) throws IOException {

		boolean result=true, uploadFile=false;
		
		try {
			File folder = new File(localDir);
			String localFileName="";
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {
					continue;
				}

				localFileName = fileEntry.getName();
				uploadFile = ifStartProcess(localFileName,pattern );

				if (uploadFile) {
					String fullPathLocalFileName = Paths.get(localDir, localFileName).toString();
					String remoteFileName = localFileName;

					result = uploadFile(fullPathLocalFileName, remoteFileName );
				}

			}

		}
		catch( Exception e) {
			result = false;
		}
		finally {
		}
		return result;
	}

	
	/**
	 * 更改檔案名稱
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/11/14
     * @param remoteDir : 遠端目錄      
	 * @param sP_SourceFileName : 舊檔名
	 * @param sP_TargetFileName : 新檔名
	 * @return
	 */
	public boolean renameFile(String remoteDir, String sourceFileName, String targetFileName) {

		boolean result = true;
	    try {
	        command.rename(sourceFileName, targetFileName);
	    } catch (Exception e) {
	    	result = false;
	    } finally {
	    }
	    
	    return result;
	}
	/**
     * 下載檔案
     * @author  Howard Chang
     * @version 1.0
     * @since   2018/07/03          
     * @param remotePath :remote path
     * @param localPath : local path
     * @param pattern :
     * 	      sP_Pattern = "*txt"; // 取回 檔名末三碼 = "txt" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg*"; // 取回 檔名前三碼 = "msg" 的檔案 ( 有分大小寫 )
			  sP_Pattern = "msg_07.txt"; // 取回 檔名  = "msg_07.txt" 的檔案
 			  sP_Pattern = "_0?"; // 取回 檔名含有 = "_0" 的檔案
 			  sP_Pattern = "Msg*jar"; //取回 檔名前三碼 = "Meg" and 檔名末三碼 = "jar" 的檔案 ( 有分大小寫 )
  
     * @return 如果成功return true，否則return  false
     * 
    */

	public boolean mgetFile(String remotePath, String localPath, String pattern) throws IOException {

		boolean result =true, downloadFile=false;
		String remoteFileName = "";
		SftpATTRS dirstat = null;
		clearProcessedFileList();
		try {

//			 command.cd(sP_RemotePath);
			 Vector filelist = listFileInDir();
			 for(int i=0; i<filelist.size();i++){
				 
				 remoteFileName = filelist.get(i).toString();

				 dirstat = command.stat(remoteFileName);
				 
				 if (dirstat.isDir()) {
					 continue;
				 }
				 
				 downloadFile = ifStartProcess(remoteFileName, pattern);

				 if (downloadFile) {

					 try {
						 String fullPathLocalFileName = Paths.get(localPath, remoteFileName).toString();
						 result = downloadFile(remoteFileName, fullPathLocalFileName);
					 } catch (Exception e) {
						 result = false;
					 } finally {
						// TODO: handle finally clause
					}
					 
				 }
			}
			
		} catch (Exception e) {
			result = false;
		} finally {
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

		boolean result =true, deleteFile=false;
		String remoteFileName = "";
		SftpATTRS dirstat = null;
		clearProcessedFileList();
		try {

//			 command.cd(sP_RemotePath);
			 Vector filelist = listFileInDir();
			 for(int i=0; i<filelist.size();i++){
				 
				 remoteFileName = filelist.get(i).toString();

				 dirstat = command.stat(remoteFileName);
				 
				 if (dirstat.isDir()) {
					 continue;
				 }
				 
				 deleteFile = ifStartProcess(remoteFileName, pattern);
				 if (deleteFile) {

					 try {
						 result =deleteRemoteFile(remoteFileName);
					 } catch (Exception e) {
						 result = false;
					 } finally {
						// TODO: handle finally clause
					}
					 
				 }
			}
			
		} catch (Exception e) {
			result = false;
		} finally {
		}
		return result;
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
		
		boolean bL_Result = true;
		try (FileInputStream inputSrr = new FileInputStream(localFilePath);){
			
			command.put(inputSrr, remotePath,FTP.ASCII_FILE_TYPE);
			addFtpFileName(localFilePath);
			
		} catch (SftpException e) {
			e.printStackTrace();
			bL_Result = false;
		}
		return bL_Result;
	}

	
	public boolean uploadFile(String localFilePath, String remotePath, int fileType) throws IOException {
		
		boolean bL_Result = true;
		try(FileInputStream inputSrr = new FileInputStream(localFilePath);) {
			
			if (fileType==1)
				command.put(inputSrr, remotePath,FTP.ASCII_FILE_TYPE);
			else 
				command.put(inputSrr, remotePath,FTP.BINARY_FILE_TYPE);


			addFtpFileName(localFilePath);
			
		} catch (SftpException e) {
			e.printStackTrace();
			bL_Result = false;
		} 
		return bL_Result;
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

	public boolean changeDir(String remotePath) throws Exception {
		try {
			command.cd(remotePath);
		} catch (SftpException e) {
			return false;
		}
		return true;
	}

	
		
	private boolean isARemoteDirectory(String path) {
		try {
			return command.stat(path).isDir();
		} catch (SftpException e) {
			e.printStackTrace();
		}
		return false;
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
		String result = "";
		try {
			result = command.pwd();
		} catch (Exception e) {
			e.printStackTrace();
			result = "";
		}
		return result;
	}

}