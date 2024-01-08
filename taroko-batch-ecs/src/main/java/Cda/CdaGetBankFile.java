/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 *  109-10-08  V1.00.01  Zuwei       fix code scan issue      *
*  109-10-19  V1.00.02    shiyuqi       updated for project coding standard     * 
 ******************************************************************************/
package Cda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;


import com.CommCrd;
import com.tcb.ap4.tool.Decryptor;


public class CdaGetBankFile {

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
		
		//String path = "C:\\Credit-Aid-Name\\";
		String path = properties.getProperty("temporary.path");
		
		File deleteFile = new File(verifyPath(path + "TEMPFILE"));
		deleteFile.delete();
		
		String tempName = "PSMP[AD].*";
		File[] file = getStartWithNameFile(path, tempName);
		
		if (file.length == 0) {
			System.out.println("no file");
		}else
		for (File tempFile : file) {
		
		String fileName = tempFile.getName();

		String userId = properties.getProperty("userid");
//		String password = properties.getProperty("password");
        Decryptor decryptor = new Decryptor();
        String password = decryptor.doDecrypt(attributes.getProperty("cr.credit.aid"));
		String getXmlPath = properties.getProperty("GetBankFile.xml");
		
				        
		CdaModGetBankFile modGetBankFile = new CdaModGetBankFile(getXmlPath);
		modGetBankFile.setXMLAttr(fileName,userId,password);
        
		
		String urlPath = properties.getProperty("url.path");
		 HttpClient infapiclient = HttpClientBuilder.create().useSystemProperties().build();
          
        //Entity
        //fix issue "Path Manipulation" 2020/09/16 Zuwei
        String postentitystring = new Scanner
        		(new File(verifyPath(properties.getProperty("GetBankFile.xml")))).useDelimiter("\\A").next(); 
        HttpEntity infapiclientpostentity = new StringEntity(postentitystring);
        
        //Request
        HttpPost infapiclientpost =  new HttpPost (urlPath);
        infapiclientpost.setEntity(infapiclientpostentity);
        infapiclientpost.setHeader("Accept", "application/soap+xml");
        infapiclientpost.setHeader("content-type", "application/soap+xml");
        
        
        
        //Response
        HttpResponse infresponse = infapiclient.execute(infapiclientpost);
        int code = infresponse.getStatusLine().getStatusCode();
        
        HttpEntity responseEntity = infresponse.getEntity();
        
        InputStream in = responseEntity.getContent();
        String theString = IOUtils.toString(in, StandardCharsets.UTF_8);
        int index = theString.indexOf("<GetBankFileResult>");
        theString = theString.substring(index + 19 );
 	    index = theString.indexOf("</GetBankFileResult>");
 	    theString = theString.substring(0,index);
// 	    System.out.println(theString);
 	    System.out.println("download file:" + " " + fileName);
         
 		
         // decodeBase64
         
 		byte[] decodedBytes = Base64.decodeBase64(theString);
 		//FileUtils.writeByteArrayToFile(new File("C:\\Credit-Aid-Return\\" + fileName + ".rar"), decodedBytes);
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
 		FileUtils.writeByteArrayToFile(new File(verifyPath(properties.getProperty("decompress.path") + fileName)), decodedBytes);
 		
 		//File fileNameTemp = new File("C:\\Credit-Aid-Name\\" + fileName + ".txt");
		File fileNameTemp = new File(verifyPath(path + fileName));
         fileNameTemp.delete();
 		
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

