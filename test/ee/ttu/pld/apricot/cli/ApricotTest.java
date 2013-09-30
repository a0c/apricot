package ee.ttu.pld.apricot.cli;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class ApricotTest {

	@Test
	public void testAssertainExample() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

		String xmlPath = "./classes/test/apricot/ee/ttu/pld/apricot/cli/Assertain_example_in.xml";
		File xmlFile = new File(new File(xmlPath).toURI().getPath());
		File outXmlFile = new File(xmlFile.getParent(), "OUT_Assertain_example_in.xml");

		try {

			assertTrue(xmlFile.exists());
			if (outXmlFile.exists()) {
				assertTrue(outXmlFile.delete());
				assertTrue(!outXmlFile.exists());
			}

			Document xml = Apricot.readXml(xmlFile.getAbsolutePath());

			assertTrue(findResults(xml, "\'coverage\'") == 0);
			assertTrue(findResults(xml, "\'diagnosis\'") == 0);

			long oldLength = xmlFile.length();
			new Apricot(new String[]{xmlFile.getAbsolutePath(), outXmlFile.getAbsolutePath()});
			long newLength = outXmlFile.length();

			assertTrue(newLength > oldLength);

			xml = Apricot.readXml(outXmlFile.getAbsolutePath());

			assertTrue(findResults(xml, "\'coverage\'") == 1);
			assertTrue(findResults(xml, "\'diagnosis\'") == 1);

		} finally {
			assertTrue(outXmlFile.delete());
		}
	}

	private int findResults(Document xml, String actionName) throws XPathExpressionException {

		int count = 0;

		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile("apricot//action[@name=" + actionName + "]");
		NodeList covNodes = (NodeList) expr.evaluate(xml, XPathConstants.NODESET);

		for (int i = 0, n = covNodes.getLength(); i < n; i++) {

			Node node = covNodes.item(i);
			Object resultNode = xPath.compile("result").evaluate(node, XPathConstants.NODE);
			if (resultNode != null) {
				count++;
			}
		}

		return count;
	}
}
