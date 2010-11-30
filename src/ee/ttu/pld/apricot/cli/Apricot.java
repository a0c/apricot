package ee.ttu.pld.apricot.cli;

import base.SourceLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ui.BusinessLogicCoverageAnalyzer;
import ui.FileDependencyResolver;
import ui.base.HLDD2VHDLMapping;
import ui.base.NodeItem;
import ui.io.CoverageReader;
import ui.io.HLDD2VHDLMappingReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class Apricot {

	public Apricot(String[] args) {

		if (args.length == 0) {
			System.out.println("ERROR: request XML file is not specified");
			return;
		}

		try {

			String fileName = args[0];

			Document xml = readXml(fileName);

			Collection<Request> requests = buildRequests(xml);

			String libPath = args.length == 2 ? args[1] : null;
			new BusinessLogicCoverageAnalyzer(requests, libPath);

			buildResponses(requests, xml);

			writeXml(xml, fileName);

		} catch (IOException e) {
			System.out.println("ERROR when reading XML: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println("ERROR when reading XML: " + e.getMessage());
		} catch (SAXException e) {
			System.out.println("ERROR when reading XML: " + e.getMessage());
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (TransformerConfigurationException e) {
			System.out.println("ERROR when writing XML: " + e.getMessage());
		} catch (TransformerException e) {
			System.out.println("ERROR when writing XML: " + e.getMessage());
		}

	}

	private void writeXml(Document xml, String fileName) throws TransformerException {

		Source xmlSource = new DOMSource(xml);

		Result result = new StreamResult(new File(fileName));

		Transformer transformer = TransformerFactory.newInstance().newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		transformer.transform(xmlSource, result);
	}

	private void buildResponses(Collection<Request> requests, Document xml) {

		for (Request request : requests) {

			if (request instanceof CoverageRequest) {
				CoverageRequest coverageRequest = (CoverageRequest) request;

				if (coverageRequest.isBroken()) {
					continue;
				}

				Element result = xml.createElement("result");

				File covFile = FileDependencyResolver.deriveCovFile(coverageRequest.getHlddFile());
				File mapFile = FileDependencyResolver.deriveMapFile(covFile);
				HLDD2VHDLMapping hldd2VHDLMapping = new HLDD2VHDLMappingReader(mapFile).getMapping();
				CoverageReader coverageReader = new CoverageReader(covFile);

				if (coverageRequest.isNodeRequested()) {

					Element coverage = xml.createElement("coverage");
					coverage.setAttribute("metric", "node");
					result.appendChild(coverage);

					Collection<NodeItem> uncoveredNodeItems = coverageReader.getUncoveredNodeItems();

					SourceLocation uncoveredSources = hldd2VHDLMapping.getSourceFor(uncoveredNodeItems);

					for (File sourceFile : uncoveredSources.getFiles()) {

						Element file = xml.createElement("file");
						file.setAttribute("name", sourceFile.getName());
						coverage.appendChild(file);

						Collection<Integer> lines = uncoveredSources.getLinesForFile(sourceFile);
						for (Integer line : lines) {
							Element lineEl = xml.createElement("line");
							lineEl.setTextContent(line.toString());
							file.appendChild(lineEl);
						}
					}
				}

				Node requestNode = coverageRequest.getRequestNode();

				requestNode.appendChild(result);

			}
		}
	}

	private Collection<Request> buildRequests(Document xml) throws XPathExpressionException {

		Collection<Request> requests = new LinkedList<Request>();

		requests.addAll(buildCoverageRequests(xml));

		return requests;
	}

	private Collection<Request> buildCoverageRequests(Document xml) throws XPathExpressionException {

		Collection<Request> requests = new LinkedList<Request>();

		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile("apricot//action[@name='coverage']");
		NodeList covNodes = (NodeList) expr.evaluate(xml, XPathConstants.NODESET);

		for (int i = 0, n = covNodes.getLength(); i < n; i++) {

			Node covNode = covNodes.item(i);
			Node requestNode = covNode;
			Object resultNode = xPath.compile("result").evaluate(requestNode, XPathConstants.NODE);
			if (resultNode != null) {
				continue;
			}

			expr = xPath.compile("settings");
			covNode = (Node) expr.evaluate(covNode, XPathConstants.NODE);

			expr = xPath.compile("design");
			NodeList designList = (NodeList) expr.evaluate(covNode, XPathConstants.NODESET);
			String design = designList.item(0).getTextContent();

			expr = xPath.compile("metric");
			NodeList metricList = (NodeList) expr.evaluate(covNode, XPathConstants.NODESET);
			Collection<String> metrics = new LinkedList<String>();
			for (int j = 0, m = metricList.getLength(); j < m; j++) {
				metrics.add(metricList.item(j).getTextContent());
			}

			requests.add(new CoverageRequest(requestNode, design, metrics));
		}
		return requests;
	}

	private Document readXml(String fileName) throws IOException, ParserConfigurationException, SAXException {

		InputStream inputStream = new FileInputStream(new File(fileName));

		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
		} finally {
			inputStream.close();
		}
	}

	public static void main(String[] args) {

		new Apricot(args);
	}
}
