/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
*  109-10-19  V1.00.01    shiyuqi       updated for project coding standard     *
 ******************************************************************************/
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

 public class CdaModPutBankFile {
	public CdaModPutBankFile(String rarPath, String storePath, String sourcePath, String putXmlPath) {
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
            filepath = CdaPutBankFile.verifyPath(filepath);
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
			Node putBankFile = doc.getElementsByTagName("PutBankFile").item(0);

			// loop the staff child node
			NodeList list = putBankFile.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {

	                   Node node = list.item(i);
			 	           
			   if ("FileName".equals(node.getNodeName())) {
				node.setTextContent(fileName);
			   }
			   
			   if ("SomeBytes".equals(node.getNodeName())) {
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

	private String getBase64FileStr(String fileName) {
		String inputName = fileName;
		//int index = fileName.lastIndexOf(".");
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
		File file = new File(CdaPutBankFile.verifyPath(sourcePath + inputName));
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
	 
}
