/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 111-01-20  V1.00.03  Justin      fix Missing XML Validation                *
* 111-01-20  V1.00.04  Justin      fix XML Entity Expansion Injection        *
******************************************************************************/
package taroko.com;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.*;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoDefineValue {

  HashMap outputHash = null;

  public void tarokoDefineValue() {}

  public void parseDefine(String xmlFile, HashMap outputHash) throws Exception {
    this.outputHash = outputHash;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
//    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
    factory.setValidating(true); // fix Missing XML Validation
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document domTree = parser.parse(xmlFile);
    printNode(domTree, "");
    return;
  }

  public void printNode(Node node, String indent) throws Exception {
    switch (node.getNodeType()) {
      case Node.DOCUMENT_NODE:
        NodeList nodes = node.getChildNodes();
        if (nodes != null) {
          for (int i = 0; i < nodes.getLength(); i++) {
            printNode(nodes.item(i), "");
          }
        }
        break;
      case Node.ELEMENT_NODE:
        String fieldName = node.getNodeName().toUpperCase();
        String showData = "";
        NamedNodeMap atts = node.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
          Node current = atts.item(i);
          if (current.getNodeName().equals("value")) {
            fieldName = fieldName.toUpperCase() + "_" + current.getNodeValue() + "_0";
            outputHash.put(fieldName, showData);
          } else if (current.getNodeName().equals("desc")) {
            showData = current.getNodeValue();
          }
        }
        NodeList children = node.getChildNodes();
        if (children != null) {
          for (int i = 0; i < children.getLength(); i++) {
            printNode(children.item(i), indent + "  ");
          }
        }
        break;
      case Node.TEXT_NODE:
        break;
    }
  }

} // End Of class TarokoDefineValue
