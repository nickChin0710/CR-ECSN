/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
*  111-12-09  V1.00.00    Ryan     program initial                          *
*  112-04-25  V1.00.01    Ryan     增加JCB   payScheme = 0300                       *
 ******************************************************************************/
package Crd;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.util.*;

import java.util.UUID;

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

 public class CrdModEndpointRequest {
	

	public CrdModEndpointRequest(String rarPath, String storePath, String sourcePath, String putXmlPath) {
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
	
	

public void setXMLAttr(String userId, String password, HceData hceData) throws InterruptedException, Exception {
		
		try {

			byte[] wssNonce = new byte[16];
			
			//  new Random().nextBytes(wssNonce);
			new SecureRandom().nextBytes(wssNonce); // fix Insecure Randomness 
						
			MessageDigest md = MessageDigest.getInstance("SHA-1"); 
			
			String tagName = "";			
			String wssUsername  = "";
			String wssCreated = "";			
		    String wssbase64Nonce = "";
		    String hClientTraceID ="";
		    String huserID = "";
		    String hTxnID ="";
		    String hClientDt = "";
		    String sysDateTime = "";
		    String sysCDate = "";
		    String sInDate = "";
		    String sInTime = "";
		    String sStanSeqNo = "";
		    String sStan = "";
		    String dpDateTime = "";
		    String payScheme = "";
		    		

		    sysDateTime ="";
		    wssUsername  = userId;
		    String wssPassword  = password;
//		    huserID = "HCEUser001";
		    huserID = "ECSUser001";
		    
		    //--從檔案中取密碼			
		    CommCrd comc = new CommCrd();
		    String confFile = AccessDAO.getEcsAcdpPath();
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
	
		    hTxnID ="HCEECS01";

			wssCreated = GetCreated();
			byte[] wssbase64Password  =  wssPassword.getBytes("UTF-8");
		    byte[] wssbase64Created =  wssCreated.getBytes("UTF-8");		    
		    wssbase64Nonce = new String(Base64.encodeBase64(wssNonce));		    
		    String wssbase64Sha1Password =  new String(Base64.encodeBase64(md.digest(mergeBytes(mergeBytes(wssNonce , wssbase64Created),wssbase64Password))));

			
		    sysDateTime = sysGmtDatetime();
		    
		    hClientTraceID =sysDateTime.substring(2,14)+"00"+sysDateTime.substring(14,17)+"EI"+hTxnID.substring(4)+"0";
		  		    
		    byte[] hbyteuserPwd  =  huserPwd.getBytes("UTF-8");
		    String hSha1userPwd = new String (Base64.encodeBase64(md.digest(hbyteuserPwd)));
			
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddhhmmss");
		    SimpleDateFormat hcd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		    hClientDt = hcd.format(sdf.parse(sysDateTime)) ;
		   
		    dpDateTime = sdf2.format(sdf2.parse(sysDateTime)) ;
		    if(hceData.hBinType.equals("J"))
		    	  payScheme = "0300";	
		    if(hceData.hBinType.equals("V"))
		    	  payScheme = "0400";
		    if(hceData.hBinType.equals("M"))
		    	  payScheme = "0500";	

		    sysCDate = new CommFunction().toChinDate(sysDateTime);
		    sInDate = sysCDate.substring(0,3) + sysCDate.substring(4,6)+ sysCDate.substring(7,9);
		    sInTime = sysDateTime.substring(8,14);
		    sStanSeqNo =sysDateTime.substring(9,17);
		    sStan = sysDateTime.substring(2,8) + sStanSeqNo;
			
			String filepath = putXmlPath; 
            filepath = verifyPath(filepath);          
                    
            
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
			try(InputStream is = CrdModEndpointRequest.class.getClassLoader().getResourceAsStream(filepath)){
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
						node.setTextContent("ECS");
					   }
				   
				   if ("UserID".equals(node.getNodeName())) {
						node.setTextContent(huserID);
					   }
				   
				   if ("UserPwd".equals(node.getNodeName())) {
						node.setTextContent(hSha1userPwd);
					   }
				   
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
				   
				   if ("MsgType".equals(node.getNodeName())) {//訊息類別 固定放“0100”
					   	node.setTextContent("0100");
					   }
				   
				   if ("PrsCode".equals(node.getNodeName())) {
						node.setTextContent("ISS01");
					   }
				   
				   if ("Stan".equals(node.getNodeName())) {//交易序號	YYMMDDnnnnnnnn由發動端產生唯一值序號, 不可重複
						node.setTextContent(sStan);
					   }
				   
				   if ("Pan".equals(node.getNodeName())) {//實體卡號 
						node.setTextContent(hceData.hCardNo);
					   }
				   
				   if ("MobileNo".equals(node.getNodeName())) {//持卡人手機號碼 
						node.setTextContent(hceData.hCellarPhone);
					   }
				   
				   if ("DpReason".equals(node.getNodeName())) {//製卡原因 固定放“07”
						node.setTextContent("07");
					   }
				   
				   if ("DpDateTime".equals(node.getNodeName())) {//製卡日期時間 
						node.setTextContent(dpDateTime); 
					   }
				   
				   if ("PayScheme".equals(node.getNodeName())) {//支付應用種類 
						node.setTextContent(payScheme); 
					   }
				   
				   if ("WalletId".equals(node.getNodeName())) {//Wallet ID
						node.setTextContent(hceData.hWalletId); 
					   }
				   
				   if ("ServiceId".equals(node.getNodeName())) {//服務代碼
						node.setTextContent(hceData.hServiceId); 
					   }
				   
				   if ("ServiceVer".equals(node.getNodeName())) {//服務版本
						node.setTextContent(hceData.hServiceVer); 
					   }
				   
				   if ("EventId".equals(node.getNodeName())) {//事件類型(EventId) 
						node.setTextContent("1"); 
					   }
				   
				   if ("SeType".equals(node.getNodeName())) {//SE類型 
						node.setTextContent("05"); 
					   }
				   
				   if ("ActCode".equals(node.getNodeName())) {//下載驗證碼(明碼)  
						node.setTextContent(hceData.hActCode); 
					   }
				   
				   if ("ActCodeEnc".equals(node.getNodeName())) {//下載驗證碼(加密)  
						node.setTextContent(hceData.hActCodeEnc); 
					   }
				   
				   if ("Track2DataEnc".equals(node.getNodeName())) {//磁軌2資料(加密)
						node.setTextContent(hceData.hTrack2DataEnc); 
					   }
				   
				   if ("PanSeq".equals(node.getNodeName())) {//PAN SEQUENCE NUMBER
						node.setTextContent("00"); 
					   }
				   
				   if ("Tpan".equals(node.getNodeName())) {//TOKEN PAN 
						node.setTextContent(hceData.hVCardNo); 
					   }
				   
				   if ("TpanExpdate".equals(node.getNodeName())) {//TPAN效期 
						node.setTextContent(hceData.hNewEndDate); 
					   }
				   
				   if ("MemberID".equals(node.getNodeName())) {//會員編號
						node.setTextContent(""); 
					   }
				   
				   if ("BarcodeType".equals(node.getNodeName())) {//條碼類型
						node.setTextContent("00"); 
					   }

//				   if ("SIR".equals(node.getNodeName())) {
//					   if (!oUTSIR.equals("")) {
//						  node.setTextContent(oUTSIR.trim()); 
//					   }
//				   }
//				   if ("ReasonCode".equals(node.getNodeName())) {
//						node.setTextContent(reasonCode); 
//					   }
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

	private String getBase64FileStr(String fileName) {
		String inputName = fileName;
		//int index = fileName.lastIndexOf(".");
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
//		File file = new File(CcaEndpointRequest.verifyPath(sourcePath + inputName));
		File file = new File(verifyPath(sourcePath + inputName));
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
