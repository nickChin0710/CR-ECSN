package Cda;

import java.io.File;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class CdaModGetBankFileXml {
	public CdaModGetBankFileXml(String getXmlPath) {
		super();
		this.getXmlPath = getXmlPath;
	}

	private String getXmlPath = "";

	public void setXMLAttr(String fileName, String userId, String password) {
		
		try {
			String filepath = getXmlPath;
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
			Node getBankFileNew = doc.getElementsByTagName("GetBankFile_New").item(0);

			// loop the staff child node
			NodeList list = getBankFileNew.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {

	                   Node node = list.item(i);
	           
			   if ("FileName".equals(node.getNodeName())) {
				node.setTextContent(fileName);
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
            
//          these methods for DocumentBuilderFactory not for transformerFactory
//			transformerFactory.setFeature("http://xml.org/sax/features/external-general-entities", false); 
//			transformerFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
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

}
