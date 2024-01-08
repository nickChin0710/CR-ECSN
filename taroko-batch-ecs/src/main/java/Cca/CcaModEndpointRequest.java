/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
*  110-09-24  V1.00.01    Castor     program initial                          *
*  110-09-30  V1.00.02    Justin     fix empty password and Insecure Randomness vulnerabilities       *
*  110-10-12  V1.00.03    Justin     not to overwrite the original template   *
*  110-10-28  V1.00.04    Wilson     confFile讀取的檔名、路徑更改                            
*  110-11-10  V1.00.05    Castor     Modify MQ共用Header                      *
*  110-11-16  V1.00.06    Justin     change to use getEcsAcdpPath()           *
*  110-12-13  V1.00.07    Justin     hTxnID應抓後4碼                          *
*  110-12-22  V1.00.08    Justin     msgtype 0302 -> 0100                     *
*  110-12-23  V1.00.09    Justin     trim empty spaces of the oUTSIR          *
*  112/02/15  V1.00.10    Ryan       取消EAI、CRSHCE06 新增HCEECS02             *
 ******************************************************************************/
package Cca;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.util.*;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import Dxc.Util.SecurityUtil;

import com.CommFunction;
import com.tcb.ap4.tool.Decryptor;
import com.AccessDAO;
import com.CommCrd;

 public class CcaModEndpointRequest {
	

	public CcaModEndpointRequest(String rarPath, String storePath, String sourcePath, String putXmlPath) {
		super();
		this.rarPath = rarPath;
		this.storePath = storePath;
		this.sourcePath = sourcePath;
		this.putXmlPath = putXmlPath;
	}

	private String rarPath = "";
	private String storePath = "";
	private String sourcePath = "";
	/**This path, putXmlPath, is relative to the resources folder**/
	private String putXmlPath = ""; 
	private String xmlResult = "";
	
	

public void setXMLAttr(String userId, String password,String tPan,String actionCode,String sir ,String reasonCode) throws InterruptedException, Exception {
		
		try {

			byte[] wssNonce = new byte[16];
			
			//  new Random().nextBytes(wssNonce);
			new SecureRandom().nextBytes(wssNonce); // fix Insecure Randomness 
						
			MessageDigest md = MessageDigest.getInstance("SHA-1"); 
			
			String tagName = "";			
			String wssUsername  = "";
//			String wssPassword  = ""; // fix empty password vulnerability
			String wssCreated = "";			
		    String wssbase64Nonce = "";
//		    String wssbase64Sha1Password =""; // fix empty password vulnerability
		    String hClientTraceID ="";
		    String huserID = "";
//		    String huserPwd  = ""; // fix empty password vulnerability
//		    String hSha1userPwd = ""; // fix empty password vulnerability
		    String hTxnID ="";
		    String hClientDt = "";
		    String sysDateTime = "";
		    String sysCDate = "";
		    String sInDate = "";
		    String sInTime = "";
		    String sStanSeqNo = "";
		    String sStan = "";
		    String dateTime = "";

		    sysDateTime ="";
		    wssUsername  = userId;
		    String wssPassword  = password;
//		    huserID = "HCEUser001";
		    huserID = "ECSUser001";
		    
		    //--從檔案中取密碼			
		    CommCrd comc = new CommCrd();
//			String confFile = comc.getECSHOME() + "/PKI/acdp.properties";
//		    String confFile = comc.getECSHOME() + "/conf/ecsAcdp.properties";
		    String confFile = AccessDAO.getEcsAcdpPath(); // Justin 2021/11/16 change to use getEcsAcdpPath()
			confFile = Normalizer.normalize(confFile, Normalizer.Form.NFKC);
			Properties props = new Properties();
			try (FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(confFile));) {
				props.load(fis);
				fis.close();
			}
			String huserPwd = props.getProperty("ecs.hce").trim();
			//--解密
			Decryptor decrptor = new Decryptor();
			huserPwd = decrptor.doDecrypt(huserPwd);	
	
		    hTxnID ="HCEECS02";

			wssCreated = GetCreated();
			byte[] wssbase64Password  =  wssPassword.getBytes("UTF-8");
		    byte[] wssbase64Created =  wssCreated.getBytes("UTF-8");		    
		    wssbase64Nonce = new String(Base64.encodeBase64(wssNonce));		    
		    String wssbase64Sha1Password =  new String(Base64.encodeBase64(md.digest(mergeBytes(mergeBytes(wssNonce , wssbase64Created),wssbase64Password))));

			
		    sysDateTime = sysGmtDatetime();
		    dateTime = sysDatetime();
		    
//		    hClientTraceID =sysDateTime.substring(2,14)+"00"+sysDateTime.substring(14,17)+"HC"+hTxnID.substring(5)+"0";
		    
		    // Justin 2021/12/13 hTxnID應抓後4碼
//		    hClientTraceID =sysDateTime.substring(2,14)+"00"+sysDateTime.substring(14,17)+"EI"+hTxnID.substring(5)+"0";
		    hClientTraceID =sysDateTime.substring(2,14)+"00"+sysDateTime.substring(14,17)+"EI"+hTxnID.substring(4)+"0";
		  		    
		    byte[] hbyteuserPwd  =  huserPwd.getBytes("UTF-8");
		    String hSha1userPwd = new String (Base64.encodeBase64(md.digest(hbyteuserPwd)));
			
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		    SimpleDateFormat hcd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		    hClientDt = hcd.format(sdf.parse(sysDateTime)) ;
		    
		   		    
		    sysCDate = new CommFunction().toChinDate(sysDateTime);
		    sInDate = sysCDate.substring(0,3) + sysCDate.substring(4,6)+ sysCDate.substring(7,9);
		    sInTime = sysDateTime.substring(8,14);
		    sStanSeqNo = sysDateTime.substring(9,17);
		    sStan = sysDateTime.substring(2,8) + sStanSeqNo;
			
			String filepath = putXmlPath;
            // fix issue "Path Manipulation" 2020/09/16 Zuwei
//          filepath = CcaEndpointRequest.verifyPath(filepath);    
            filepath = CcaB002.verifyPath(filepath);          
                    
            
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            // fix issue "XML External Entity Injection" 2020/09/16 Zuwei
            docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            docFactory.setXIncludeAware(false);
            docFactory.setExpandEntityReferences(false);
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//			Document doc = docBuilder.parse(filepath);
			Document doc = null;
			try(InputStream is = CcaModEndpointRequest.class.getClassLoader().getResourceAsStream(filepath)){
				doc = docBuilder.parse(is);
			}
			
			// Get the root element
			Node body = doc.getFirstChild();

			for (int j = 1 ; j <= 3; j++) {
				// Get the endpointRequest element by tag name directly
				switch (j) {
		       
		         case 1:		        	
		            tagName ="wsse:UsernameToken";
		            break; 

		         case 2:
		            tagName ="Header";
		            break; 

		         case 3:
		            tagName ="SvcRq";
		            break; 
				}				
				
				Node endpointRequest = doc.getElementsByTagName(tagName).item(0);
        
		        if(tagName.equals("wsse:UsernameToken")) {
		           Node value = endpointRequest.getAttributes().getNamedItem("wsu:Id");
		           value.setNodeValue(createSecureId());
		        }

				// loop the staff child node
				NodeList list = endpointRequest.getChildNodes();
	
				for (int i = 0; i < list.getLength(); i++) {
	
		                   Node node = list.item(i);
				 
				   // wss
				   if ("wsse:Username".equals(node.getNodeName())) {
						node.setTextContent(wssUsername);
					   }				   
	
				   if ("wsse:Password".equals(node.getNodeName())) {
						node.setTextContent(wssbase64Sha1Password);
					   }				   
				   
				   if ("wsse:Nonce".equals(node.getNodeName())) {
						node.setTextContent(wssbase64Nonce);
					   }
				   
				   if ("wsu:Created".equals(node.getNodeName())) {
						node.setTextContent(wssCreated);
					   }
				   
				// Header
				   if ("ClientTraceID".equals(node.getNodeName())) {
						node.setTextContent(hClientTraceID); 
					   }
				   	
				   if ("SrcSystemID".equals(node.getNodeName())) {
//						node.setTextContent("HCE");
						node.setTextContent("ECS");
					   }
				   
				   if ("UserID".equals(node.getNodeName())) {
						node.setTextContent(huserID);
					   }
				   
				   if ("UserPwd".equals(node.getNodeName())) {
						node.setTextContent(hSha1userPwd);
					   }
				   //SessionID 先不放值
//				   if ("SessionID".equals(node.getNodeName())) {
//						node.setTextContent("EAI");
//						node.setTextContent(" ");
//					   }
				   
				   if ("TxnID".equals(node.getNodeName())) {
						node.setTextContent(hTxnID);
					   }
				   
				   if ("ClientDt".equals(node.getNodeName())) {
						node.setTextContent(hClientDt);
					   }
				// SvcRq
				   if ("InDate".equals(node.getNodeName())) {
						node.setTextContent(sInDate);
					   }
				   	
				   if ("InTime".equals(node.getNodeName())) {
						node.setTextContent(sInTime);
					   }
				   
				   if ("MsgType".equals(node.getNodeName())) {
					   // Justin 2021/12/22: msgtype 0302 -> 0100
//						node.setTextContent("0302");
					   node.setTextContent("0100");
					   }
				   
				   if ("PrsCode".equals(node.getNodeName())) {
						node.setTextContent("ISS02");
					   }
				   
				   if ("Stan".equals(node.getNodeName())) {
						node.setTextContent(sStan);
					   }
				   
//				   if ("TPan".equals(node.getNodeName())) {
//						node.setTextContent(tPan); 
//					   }
				   
				   if ("SIR".equals(node.getNodeName())) {
						node.setTextContent(sir); 
				   }
				   
				   if ("Action".equals(node.getNodeName())) {
						node.setTextContent(actionCode); 
				   }
				   
				   if ("Reason".equals(node.getNodeName())) {
						node.setTextContent(reasonCode); 
				   }
				   
				   if ("DateTime".equals(node.getNodeName())) {
						node.setTextContent(dateTime); 
				   }
				}
			
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
            // fix issue "XML External Entity Injection" 2020/09/16 Zuwei
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,"");
           
                       
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
//			StreamResult result = new StreamResult(new File(filepath));
			try(StringWriter sw = new StringWriter();){
				StreamResult result = new StreamResult(sw);
				transformer.transform(source, result);
				setXmlResult(sw.toString());
			}
			
			
		   } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (TransformerException tfe) {
			tfe.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   }

	}

public void setXMLAttrP2(String userId, String password,String tPan,String reasonCode,String oUTSIR) throws InterruptedException, Exception {
	
	try {

		byte[] wssNonce = new byte[16];
		
		//  new Random().nextBytes(wssNonce);
		new SecureRandom().nextBytes(wssNonce); // fix Insecure Randomness 
					
		MessageDigest md = MessageDigest.getInstance("SHA-1"); 
		
		String tagName = "";			
		String wssUsername  = "";
//		String wssPassword  = ""; // fix empty password vulnerability
		String wssCreated = "";			
	    String wssbase64Nonce = "";
//	    String wssbase64Sha1Password =""; // fix empty password vulnerability
	    String hClientTraceID ="";
	    String huserID = "";
//	    String huserPwd  = ""; // fix empty password vulnerability
//	    String hSha1userPwd = ""; // fix empty password vulnerability
	    String hTxnID ="";
	    String hClientDt = "";
	    String sysDateTime = "";
	    String sysCDate = "";
	    String sInDate = "";
	    String sInTime = "";
	    String sStanSeqNo = "";
	    String sStan = "";
	    		

	    sysDateTime ="";
	    wssUsername  = userId;
	    String wssPassword  = password;
//	    huserID = "HCEUser001";
	    huserID = "ECSUser001";
	    
	    //--從檔案中取密碼			
	    CommCrd comc = new CommCrd();
//		String confFile = comc.getECSHOME() + "/PKI/acdp.properties";
//	    String confFile = comc.getECSHOME() + "/conf/ecsAcdp.properties";
	    String confFile = AccessDAO.getEcsAcdpPath(); // Justin 2021/11/16 change to use getEcsAcdpPath()
		confFile = Normalizer.normalize(confFile, Normalizer.Form.NFKC);
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(SecurityUtil.verifyPath(confFile));) {
			props.load(fis);
			fis.close();
		}
		String huserPwd = props.getProperty("ecs.hce").trim();
		//--解密
		Decryptor decrptor = new Decryptor();
		huserPwd = decrptor.doDecrypt(huserPwd);	

	    hTxnID ="CRSHCE06";

		wssCreated = GetCreated();
		byte[] wssbase64Password  =  wssPassword.getBytes("UTF-8");
	    byte[] wssbase64Created =  wssCreated.getBytes("UTF-8");		    
	    wssbase64Nonce = new String(Base64.encodeBase64(wssNonce));		    
	    String wssbase64Sha1Password =  new String(Base64.encodeBase64(md.digest(mergeBytes(mergeBytes(wssNonce , wssbase64Created),wssbase64Password))));

		
	    sysDateTime = sysGmtDatetime();
	    
//	    hClientTraceID =sysDateTime.substring(2,14)+"00"+sysDateTime.substring(14,17)+"HC"+hTxnID.substring(5)+"0";
	    
	    // Justin 2021/12/13 hTxnID應抓後4碼
//	    hClientTraceID =sysDateTime.substring(2,14)+"00"+sysDateTime.substring(14,17)+"EI"+hTxnID.substring(5)+"0";
	    hClientTraceID =sysDateTime.substring(2,14)+"00"+sysDateTime.substring(14,17)+"EI"+hTxnID.substring(4)+"0";
	  		    
	    byte[] hbyteuserPwd  =  huserPwd.getBytes("UTF-8");
	    String hSha1userPwd = new String (Base64.encodeBase64(md.digest(hbyteuserPwd)));
		
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	    SimpleDateFormat hcd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	    hClientDt = hcd.format(sdf.parse(sysDateTime)) ;
	    
	   		    
	    sysCDate = new CommFunction().toChinDate(sysDateTime);
	    sInDate = sysCDate.substring(0,3) + sysCDate.substring(4,6)+ sysCDate.substring(7,9);
	    sInTime = sysDateTime.substring(8,14);
	    sStanSeqNo =sysDateTime.substring(9,14)+sysDateTime.substring(15,17);
	    sStan = sysDateTime.substring(2,8) + sStanSeqNo;
		
		String filepath = putXmlPath;
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
//      filepath = CcaEndpointRequest.verifyPath(filepath);    
        filepath = CcaB002.verifyPath(filepath);          
                
        
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        // fix issue "XML External Entity Injection" 2020/09/16 Zuwei
        docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        docFactory.setXIncludeAware(false);
        docFactory.setExpandEntityReferences(false);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//		Document doc = docBuilder.parse(filepath);
		Document doc = null;
		try(InputStream is = CcaModEndpointRequest.class.getClassLoader().getResourceAsStream(filepath)){
			doc = docBuilder.parse(is);
		}
		
		// Get the root element
		Node body = doc.getFirstChild();

		for (int j = 1 ; j <= 3; j++) {
			// Get the endpointRequest element by tag name directly
			switch (j) {
	       
	         case 1:		        	
	            tagName ="wsse:UsernameToken";
	            break; 

	         case 2:
	            tagName ="Header";
	            break; 

	         case 3:
	            tagName ="SvcRq";
	            break; 
			}				
			
			Node endpointRequest = doc.getElementsByTagName(tagName).item(0);
    
	        if(tagName.equals("wsse:UsernameToken")) {
	           Node value = endpointRequest.getAttributes().getNamedItem("wsu:Id");
	           value.setNodeValue(createSecureId());
	        }

			// loop the staff child node
			NodeList list = endpointRequest.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {

	                   Node node = list.item(i);
			 
			   // wss
			   if ("wsse:Username".equals(node.getNodeName())) {
					node.setTextContent(wssUsername);
				   }				   

			   if ("wsse:Password".equals(node.getNodeName())) {
					node.setTextContent(wssbase64Sha1Password);
				   }				   
			   
			   if ("wsse:Nonce".equals(node.getNodeName())) {
					node.setTextContent(wssbase64Nonce);
				   }
			   
			   if ("wsu:Created".equals(node.getNodeName())) {
					node.setTextContent(wssCreated);
				   }
			   
			// Header
			   if ("ClientTraceID".equals(node.getNodeName())) {
					node.setTextContent(hClientTraceID); 
				   }
			   	
			   if ("SrcSystemID".equals(node.getNodeName())) {
//					node.setTextContent("HCE");
					node.setTextContent("ECS");
				   }
			   
			   if ("UserID".equals(node.getNodeName())) {
					node.setTextContent(huserID);
				   }
			   
			   if ("UserPwd".equals(node.getNodeName())) {
					node.setTextContent(hSha1userPwd);
				   }
			   //SessionID 先不放值
//			   if ("SessionID".equals(node.getNodeName())) {
//					node.setTextContent("EAI");
//					node.setTextContent(" ");
//				   }
			   
			   if ("TxnID".equals(node.getNodeName())) {
					node.setTextContent(hTxnID);
				   }
			   
			   if ("ClientDt".equals(node.getNodeName())) {
					node.setTextContent(hClientDt);
				   }
			// SvcRq
			   if ("InDate".equals(node.getNodeName())) {
					node.setTextContent(sInDate);
				   }
			   	
			   if ("InTime".equals(node.getNodeName())) {
					node.setTextContent(sInTime);
				   }
			   
			   if ("MsgType".equals(node.getNodeName())) {
				   // Justin 2021/12/22: msgtype 0302 -> 0100
//					node.setTextContent("0302");
				   node.setTextContent("0100");
				   }
			   
			   if ("PrsCode".equals(node.getNodeName())) {
					node.setTextContent("TGW21");
				   }
			   
			   if ("Stan".equals(node.getNodeName())) {
					node.setTextContent(sStan);
				   }
			   
			   if ("TPan".equals(node.getNodeName())) {
					node.setTextContent(tPan); 
				   }
			   
			   if ("SIR".equals(node.getNodeName())) {
				   if (!oUTSIR.equals("")) {
					// Justin 2021/12/23: trim empty spaces of the oUTSIR
//					node.setTextContent(oUTSIR); 
					   node.setTextContent(oUTSIR.trim()); 
				   }
			   }
			   if ("ReasonCode".equals(node.getNodeName())) {
					node.setTextContent(reasonCode); 
				   }
			}
		
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // fix issue "XML External Entity Injection" 2020/09/16 Zuwei
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,"");
       
                   
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
//		StreamResult result = new StreamResult(new File(filepath));
		try(StringWriter sw = new StringWriter();){
			StreamResult result = new StreamResult(sw);
			transformer.transform(source, result);
			setXmlResult(sw.toString());
		}
		
		
	   } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	   } catch (TransformerException tfe) {
		tfe.printStackTrace();
	   } catch (IOException ioe) {
		ioe.printStackTrace();
	   } catch (SAXException sae) {
		sae.printStackTrace();
	   }

}

	private String getBase64FileStr(String fileName) {
		String inputName = fileName;
		//int index = fileName.lastIndexOf(".");
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
//		File file = new File(CcaEndpointRequest.verifyPath(sourcePath + inputName));
		File file = new File(CcaB002.verifyPath(sourcePath + inputName));
        String encodedBase64 = null;
        // fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
		try (FileInputStream fileInputStreamReader = new FileInputStream(file);) {
			byte[] bytes = new byte[(int)file.length()];
	        fileInputStreamReader.read(bytes);
	        encodedBase64 = new String(Base64.encodeBase64(bytes));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return encodedBase64;
	}
	
	void deleteFile(String fileName) {
		 String inputName = fileName;
		 File file = new File(sourcePath + inputName);
		 boolean b = file.delete();
		 if (b){
			 System.out.println("file delete successfully");
		 }
		 else {
			 System.out.println("failed delete file");
		 }
	 }
	
	public static String GetCreated(){
		SimpleDateFormat dateFormatUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dataTimeString = dateFormatUtc.format(new Date());
		return dataTimeString;
		 
	}
			
	public String sysGmtDatetime() {
		 SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		 form1.setTimeZone(TimeZone.getTimeZone("GMT") );
		 return form1.format( new Date());
	}
	
	public String sysDatetime() {
		 SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddhhmmss");
		 return form1.format( new Date());
	}
		
	 public static byte[] mergeBytes(byte[] data1, byte[] data2) {
	        byte[] result = new byte[data1.length + data2.length];
	        System.arraycopy(data1, 0, result, 0, data1.length);
	        System.arraycopy(data2, 0, result, data1.length, data2.length);
	        return result;
	 }
	 
	

    public String createSecureId() {
        return "UsernameToken-" + getUUID() ;
    }
    
    public static String getUUID() {
    	  UUID uuid = UUID.randomUUID();
    	  String str = uuid.toString();
    	  String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
    	  return temp;
    	 }
    
    public void setXmlResult(String xmlResult) {
    	this.xmlResult = xmlResult;
    }
    
    public String getXmlResult() {
    	return this.xmlResult;
    }
		
	 

			 
}
