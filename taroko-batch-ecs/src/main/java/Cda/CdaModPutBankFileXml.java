package Cda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CdaModPutBankFileXml {
	 public CdaModPutBankFileXml(String rarPath, String storePath, String sourcePath, String putXmlPath) {
			super();
			this.rarPath = rarPath;
			this.storePath = storePath;
			this.sourcePath = sourcePath;
			this.putXmlPath = putXmlPath;
		}

		private String rarPath = "";
		private String storePath = "";
		private String sourcePath = "";
		private String putXmlPath = "";
		

	public void setXMLAttr(String fileName, String userId, String password) throws InterruptedException, Exception {
			
			try {
				String filepath = putXmlPath;
	            // fix issue "Path Manipulation" 2020/09/16 Zuwei
	            filepath = CdaPutBankFileNew.verifyPath(filepath);
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	            // fix issue "XML External Entity Injection" 2020/09/16 Zuwei
	            docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
	            docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	            docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	            docFactory.setXIncludeAware(false);
	            docFactory.setExpandEntityReferences(false);
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(filepath);

				// Get the root element
				Node body = doc.getFirstChild();


				// Get the GetBankFileNew element by tag name directly
				Node putBankFileNew = doc.getElementsByTagName("PutBankFile_New").item(0);

				// loop the staff child node
				NodeList list = putBankFileNew.getChildNodes();

				for (int i = 0; i < list.getLength(); i++) {

		                   Node node = list.item(i);
				 	           
				   if ("FileName".equals(node.getNodeName())) {
					node.setTextContent(fileName);
				   }
				   
				   if ("SomeBytes".equals(node.getNodeName())) {
					    convertTxtToRar(fileName, password);
						node.setTextContent(getBase64FileStr(fileName));
					   }
				   
				   if ("userId".equals(node.getNodeName())) {
					node.setTextContent(userId);
				   }
				   
				   if ("password".equals(node.getNodeName())) {
					node.setTextContent(password);
				   }

				}

				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            // fix issue "XML External Entity Injection" 2020/09/16 Zuwei
	            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
	            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
	            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,"");
	            
//	            these methods for DocumentBuilderFactory not for transformerFactory
	            
//				transformerFactory.setFeature("http://xml.org/sax/features/external-general-entities", false); 
//				transformerFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	                       
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(filepath));
				transformer.transform(source, result);
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

		private void convertTxtToRar(String fileName, String password) throws InterruptedException, Exception {
			 String inputName = fileName;
			 int index = fileName.lastIndexOf(".rar");
//			 String rarPath = "C:\\Program Files\\WinRAR";
//			 String storePath = "C:\\Credit-Aid-BAK\\";
//		     String sourcePath = "C:\\";
		     String cmd = rarPath + "rar a -df -ep1 -p" + password + " " + storePath + inputName.substring(0,index) + ".rar" + " " + sourcePath + inputName.substring(0,index);
		     Process proc;
		     proc = Runtime.getRuntime().exec(cmd);
		     if (proc.waitFor() != 0) {
		     	throw new Exception("error" + " " + fileName + " " + "error reason" + proc.exitValue());
		     }
		     else {
		    	 System.out.println("success");
		    	 System.out.println(fileName);
		     }
			
		}

		private String getBase64FileStr(String fileName) {
			String inputName = fileName;
			int index = fileName.lastIndexOf(".");
	        // fix issue "Path Manipulation" 2020/09/16 Zuwei
			File file = new File(CdaPutBankFileNew.verifyPath(storePath + inputName.substring(0,index) + ".rar" ));
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
			      file.delete();
	 
		 }

}
