package ee.ttu.pld.apricot.cli;

import base.SourceLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ui.FileDependencyResolver;
import ui.base.HLDD2VHDLMapping;
import ui.base.NodeItem;
import ui.base.VariableItem;
import ui.io.CoverageReader;
import ui.io.DiagnosisReader;
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

		if (args.length != 1) {
			System.out.println("[APRICOT] ### ERROR ###: request XML file is not specified");
			return;
		}
		String fileName = args[0];

		try {

			Document xml = readXml(fileName);

			Collection<Request> requests = buildRequests(xml);

			processRequests(requests);

			buildResponses(requests, xml);

			writeXml(xml, fileName);

			printStat(requests);

		} catch (IOException e) {
			System.out.println("[APRICOT] ### ERROR when reading XML ###: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			System.out.println("[APRICOT] ### ERROR when reading XML ###: " + e.getMessage());
		} catch (SAXException e) {
			System.out.println("[APRICOT] ### ERROR when reading XML ###: " + e.getMessage());
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (TransformerConfigurationException e) {
			System.out.println("[APRICOT] ### ERROR when writing XML ###: " + e.getMessage());
		} catch (TransformerException e) {
			System.out.println("[APRICOT] ### ERROR when writing XML ###: " + e.getMessage());
		}

	}

	private void processRequests(Collection<Request> requests) {
		for (Request request : requests) {
			new TaskRunner(request);
		}
	}

	private void printStat(Collection<Request> requests) {
		int i = 0;
		for (Request request : requests) {
			if (request.isSuccessful()) {
				i++;
			}
		}
		System.out.println("");
		System.out.println("[APRICOT] Total completed tasks: " + i);
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

			if (request.isBroken() || !request.isSuccessful()) {
				continue;
			}

			Element result = xml.createElement("result");

			Node requestNode = request.getRequestNode();

			if (request instanceof CoverageRequest) {

				fillCoverageResult(result, (CoverageRequest) request, xml);

			} else if (request instanceof DiagnosisRequest) {

				fillDiagnosisResult(result, (DiagnosisRequest) request, xml);
			}

			requestNode.appendChild(result);

		}
	}

	private void fillDiagnosisResult(Element result, DiagnosisRequest diagnosisRequest, Document xml) {

		File dgnFile = FileDependencyResolver.deriveDgnFile(diagnosisRequest.getHlddFile());
		File mapFile = FileDependencyResolver.deriveFileFrom(dgnFile, ".dgn", ".map");
		HLDD2VHDLMapping hldd2VHDLMapping = new HLDD2VHDLMappingReader(mapFile).getMapping();
		DiagnosisReader diagnosisReader = new DiagnosisReader(dgnFile);
		/* SCORE 1 */
		Element score1 = xml.createElement("score1");
		result.appendChild(score1);

		Collection<VariableItem> candidates1 = diagnosisReader.getCandidates1();
		SourceLocation source1 = hldd2VHDLMapping.getSourceFor(candidates1);

		fillSource(source1, score1, xml);
		/* SCORE 2 */
		Element score2 = xml.createElement("score2");
		result.appendChild(score2);

		Collection<VariableItem> candidates2 = diagnosisReader.getCandidates2();
		SourceLocation source2 = hldd2VHDLMapping.getSourceFor(candidates2);

		fillSource(source2, score2, xml);
	}

	private void fillCoverageResult(Element result, CoverageRequest coverageRequest, Document xml) {

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

			fillSource(uncoveredSources, coverage, xml);
		}
	}

	private void fillSource(SourceLocation source, Element parent, Document xml) {

		if (source == null) {
			return;
		}
		for (File sourceFile : source.getFiles()) {

			Element file = xml.createElement("file");
			file.setAttribute("name", sourceFile.getName());
			parent.appendChild(file);

			Collection<Integer> lines = source.getLinesForFile(sourceFile);
			for (Integer line : lines) {
				Element lineEl = xml.createElement("line");
				lineEl.setTextContent(line.toString());
				file.appendChild(lineEl);
			}
		}
	}

	private Collection<Request> buildRequests(Document xml) throws XPathExpressionException {

		Collection<Request> requests = new LinkedList<Request>();

		requests.addAll(buildCoverageRequests(xml));
		requests.addAll(buildDiagnosisRequests(xml));

		return requests;
	}

	private Collection<Request> buildDiagnosisRequests(Document xml) throws XPathExpressionException {

		Collection<Request> requests = new LinkedList<Request>();

		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile("apricot//action[@name='diagnosis']");
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

			requests.add(new DiagnosisRequest(requestNode, design));
		}

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

	static Document readXml(String fileName) throws IOException, ParserConfigurationException, SAXException {

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
