package Cda;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tcb.ap4.tool.Decryptor;

import Cda.CdaModPutBankFileXml;


public class CdaPutBankFileNew {

	public static void main(String[] args) throws Exception {
		
//		CommCrd comc = new CommCrd();

		Properties properties = new Properties();
		String configFile = "D:\\cr\\ecs\\config\\CdaConfig.properties";
		// fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
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
		 String disposeFile = "D:\\cr\\ecs\\config\\acdp.properties";
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
		
		String fileName = tempFile.getName() + ".rar";
		String userId = properties.getProperty("userid");
//		String password = attributes.getProperty("cr.credit.aid");
		Decryptor decryptor = new Decryptor();
        String password = decryptor.doDecrypt(attributes.getProperty("cr.credit.aid"));

		
		String rarPath = properties.getProperty("winRAR.path");
		String storePath = properties.getProperty("backup.path");
		String sourcePath = properties.getProperty("transfer.path");
		String putXmlPath = properties.getProperty("PutBankFileNew.xml");
		String urlPath = properties.getProperty("url.path");
		String proxyHost = properties.getProperty("proxyHost.path");
		String proxyPort = properties.getProperty("proxyPort.path");
		
		
		CdaModPutBankFileXml modPutBankFileXml = new CdaModPutBankFileXml(rarPath, storePath, sourcePath, putXmlPath);
		modPutBankFileXml.setXMLAttr(fileName,userId,password);
		
		System.setProperty("https.proxyHost", proxyHost);
		System.setProperty("https.proxyPort", proxyPort);
		
		
	
        HttpClient infapiclient = HttpClientBuilder.create().useSystemProperties().build();
          
        //Entity
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
        String putBankFile = properties.getProperty("PutBankFileNew.xml");
        String postentitystring = new Scanner
        		(new File(putBankFile)).useDelimiter("\\A").next(); 
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
        	modPutBankFileXml.deleteFile(fileName);
        }
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
