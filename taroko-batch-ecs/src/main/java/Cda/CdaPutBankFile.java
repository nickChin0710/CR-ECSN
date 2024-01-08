/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 *  109-10-08  V1.00.01  Zuwei       fix code scan issue      *
*  109-10-19  V1.00.02    shiyuqi       updated for project coding standard     *         *  
 ******************************************************************************/
package Cda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


import com.CommCrd;
import com.tcb.ap4.tool.Decryptor;

import Cda.CdaModPutBankFile;


public class CdaPutBankFile {

	public static void main(String[] args){
		
		try {
			CommCrd comc = new CommCrd();
			Properties properties = new Properties();
			String configFile = comc.getECSHOME() + "/conf/CdaConfig.properties";

			// fix issue "Unreleased Resource: Streams" 2020/10/08 Zuwei
	        try (InputStream in = new FileInputStream(configFile)) {
				  properties.load(in);
			}catch (FileNotFoundException ex) {
				ex.printStackTrace();
				return;
			}catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
	        
	        Properties attributes = new Properties();
			String disposeFile = "/PKI/acdp.properties";
			try (InputStream ins = new FileInputStream(disposeFile)) {
				attributes.load(ins);
			}catch (FileNotFoundException ex) {
				ex.printStackTrace();
				return;
			}catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
			
			String path = properties.getProperty("transfer.path");
			String tempName = "PSMQ[AD].*|TEMPFILE";
			File[] file = getStartWithNameFile(path, tempName);
			
			if (file.length == 0) {
				System.out.println("no file");
			}else
			for (File tempFile : file) {
									
//			String inputName = tempFile.getName().replace('M', 'F');
			String getFileName = tempFile.getName().replace('Q', 'P');
			
			String inputFilePath = properties.getProperty("transfer.path") + tempFile.getName();
			String outputFilePath = properties.getProperty("temporary.path") + getFileName;
	        // fix issue "Path Manipulation" 2020/09/16 Zuwei
	        inputFilePath = verifyPath(inputFilePath);
	        outputFilePath = verifyPath(outputFilePath);
			   try(
					   FileOutputStream fos = new FileOutputStream(new File(outputFilePath))
					   ){
				       Path inputPath = new File(inputFilePath).toPath();
				       Files.copy(inputPath, fos);
			   } catch (IOException e) {
				   e.printStackTrace();
			   } 
			
			String fileName = tempFile.getName();
			String userId = properties.getProperty("userid");
//			String password = properties.getProperty("password");
	        Decryptor decryptor = new Decryptor();
	        String password = decryptor.doDecrypt(attributes.getProperty("cr.credit.aid"));


			
			String rarPath = properties.getProperty("winRAR.path");
			String storePath = properties.getProperty("backup.path");
			String sourcePath = properties.getProperty("transfer.path");
			String putXmlPath = properties.getProperty("PutBankFile.xml");
			String urlPath = properties.getProperty("url.path");
					
					
			CdaModPutBankFile modPutBankFile = new CdaModPutBankFile(rarPath, storePath, sourcePath, putXmlPath);
			modPutBankFile.setXMLAttr(fileName,userId,password);
						
			
	        HttpClient infapiclient = HttpClientBuilder.create().useSystemProperties().build();
	          
	        //Entity
	        // fix issue "Path Manipulation" 2020/09/16 Zuwei
	        String pubBankFile = properties.getProperty("PutBankFile.xml");
	        String postentitystring = new Scanner
	        		(new File(pubBankFile)).useDelimiter("\\A").next(); 
	        HttpEntity infapiclientpostentity = new StringEntity(postentitystring);
	        
	        
	        
	        //Request
	        HttpPost infapiclientpost =  new HttpPost (urlPath);
	        infapiclientpost.setEntity(infapiclientpostentity);
	        infapiclientpost.setHeader("Accept", "application/soap+xml");
	        infapiclientpost.setHeader("content-type", "application/soap+xml");
	      
	        
	        //Response
	        HttpResponse infresponse = infapiclient.execute(infapiclientpost);
	        HttpEntity responseEntity = infresponse.getEntity();
	        if(responseEntity != null) {
	        	String response = EntityUtils.toString(responseEntity,"UTF-8");
	        	System.out.println(response);
	        	modPutBankFile.deleteFile(fileName);
	        }
		}
			
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
		
}


	private static File[] getStartWithNameFile(String path, String tempName) {
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
		File src = new File(verifyPath(path));
		File[] listFiles  = new File[]{};
		if(src.isDirectory()){	
			listFiles = src.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
				
					return new File(dir, name).isFile() && name.matches(tempName);
				}
				
			}
					);
		}
		return listFiles;
	}
    
    // check file name 
    public static String verifyPath(String path) {
        String tempStr = path;
        while (tempStr.indexOf("..\\") >= 0 || tempStr.indexOf("../") >= 0) {
        	tempStr = tempStr.replace("..\\", ".\\");
        	tempStr = tempStr.replace("../", "./");
        }
        
        return tempStr;
	}
}

