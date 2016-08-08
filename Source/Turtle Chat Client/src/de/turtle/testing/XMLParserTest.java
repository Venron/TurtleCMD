package de.turtle.testing;

import javax.xml.parsers.*;
import java.io.File;
import org.w3c.dom.*;

public class XMLParserTest {
	public static void main(String[] args) {
		try {
			File xmlFile = new File("users.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xmlFile);
			

//			doc.getDocumentElement().normalize();

			// --> turtle-users
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			NodeList list = doc.getElementsByTagName("user");
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				System.out.println("Node name: " + node.getNodeName());
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					System.out.println("Role Attribute: " + e.getAttribute("role"));
					System.out.println("Username: " + e.getElementsByTagName("username").item(0));
					System.out.println("Username: " + e.getElementsByTagName("password").item(0).getNodeValue());
				}
			}
		} catch (Exception e) {
			System.out.println("XMLParserTest.main()");
		}
	}
}
