package Cda;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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










public class CdaGetBankFileNew {
	
	
	
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
				
		//String path = "C:\\Credit-Aid-Name\\";
		String path = properties.getProperty("temporary.path");
		String tempName = "PSMP[AD].*";
		File[] file = getStartWithNameFile(path, tempName);
				
		if (file.length == 0) {
					System.out.println("no file");
				}else
					
		for (File tempFile : file) {
				
//			    int nameIndex = tempFile.getName().lastIndexOf(".txt");
//				String fileName = tempFile.getName().substring(0, nameIndex);			
			    String fileName = tempFile.getName();
				String userId = properties.getProperty("userid");
//				String password = attributes.getProperty("cr.credit.aid");
				Decryptor decryptor = new Decryptor();
		        String password = decryptor.doDecrypt(attributes.getProperty("cr.credit.aid"));
				String getXmlPath = properties.getProperty("GetBankFileNew.xml");
				String proxyHost = properties.getProperty("proxyHost.path");
				String proxyPort = properties.getProperty("proxyPort.path");
				
				
//				File fileNameTemp = new File(verifyPath(path + fileName + ".txt"));
				File fileNameTemp = new File(verifyPath(path + fileName));
						        
				CdaModGetBankFileXml modGetBankFileXml = new CdaModGetBankFileXml(getXmlPath);
				modGetBankFileXml.setXMLAttr(fileName,userId,password);
		        
				System.setProperty("https.proxyHost", proxyHost);
				System.setProperty("https.proxyPort", proxyPort);
				
				String urlPath = properties.getProperty("url.path");
				HttpClient infapiclient = HttpClientBuilder.create().useSystemProperties().build();
		          
		        //Entity
		        //fix issue "Path Manipulation" 2020/09/16 Zuwei
		        String postentitystring = new Scanner
		        		(new File(verifyPath(properties.getProperty("GetBankFileNew.xml")))).useDelimiter("\\A").next(); 
		        HttpEntity infapiclientpostentity = new StringEntity(postentitystring);
		        
		        //Request
		        HttpPost infapiclientpost =  new HttpPost (urlPath);
		        infapiclientpost.setEntity(infapiclientpostentity);
		        infapiclientpost.setHeader("Accept", "application/soap+xml");
		        infapiclientpost.setHeader("content-type", "application/soap+xml");
		        
		        
		        
		        //Response
		        HttpResponse infresponse = infapiclient.execute(infapiclientpost);
		        int code = infresponse.getStatusLine().getStatusCode();
		         System.out.println(code);
//		         System.out.println(infresponse.toString());
		        
		        HttpEntity responseEntity = infresponse.getEntity();
		        
		        
		        InputStream in = responseEntity.getContent();
		        String theString = IOUtils.toString(in, StandardCharsets.UTF_8);
		        int index = theString.indexOf("<GetBankFile_NewResult>");
		        theString = theString.substring(index + 23 );
		 	    index = theString.indexOf("</GetBankFile_NewResult>");
		 	    if(index > -1) {
		 	    	
		 	    	theString = theString.substring(0,index);
			 	    //System.out.println(theString);
			         
			 		
			         // decodeBase64
			         
			 		byte[] decodedBytes = Base64.decodeBase64(theString);
			 		//FileUtils.writeByteArrayToFile(new File("C:\\Credit-Aid-Return\\" + fileName + ".rar"), decodedBytes);
			        // fix issue "Path Manipulation" 2020/09/16 Zuwei
			 		FileUtils.writeByteArrayToFile(new File(verifyPath(properties.getProperty("return.path") + fileName + ".rar")), decodedBytes); 
			 		
			 		System.out.println(fileName);
			        String rarPath = properties.getProperty("winRAR.path");
			        String sourcePath = properties.getProperty("return.path");
			        String inputPath = properties.getProperty("decompress.path");
			        String cmd = rarPath + "rar x -y -p" + password + " " + "-ibck" + " " + sourcePath + fileName + ".rar" + " " + inputPath;
			        Process proc;
			        proc = Runtime.getRuntime().exec(cmd);
			        if (proc.waitFor() != 0) {
			        	throw new Exception("error" + " " + fileName + " " + "error reason" + " " + proc.exitValue());
			        }
			        else {
			       	 System.out.println("success");
			       	 fileNameTemp.delete();
			        }
			       
		 	    	
		 	    }else {
		 	    	System.out.println("can not find file:" + fileName);
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
