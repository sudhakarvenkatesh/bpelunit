package coverage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.bpelunit.framework.control.ext.IBPELDeployer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import coverage.deployarchivetools.IDeploymentArchiveHandler;
import coverage.deployarchivetools.impl.ActiveBPELDeploymentArchiveHandler;
import coverage.instrumentation.metrics.IMetricHandler;
import coverage.instrumentation.metrics.MetricHandler;
import coverage.instrumentation.metrics.statementcoverage.Statementmetric;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileWriter;
import exception.ArchiveFileException;
import exception.BpelException;
import exception.BpelVersionException;

public class CoverageMeasurement {

	public static String initializeCoverageMeasurement(String pathToArchive,String bprFile,
			IBPELDeployer deployer) throws FileNotFoundException,
			BpelException, BpelVersionException, ArchiveFileException {

		IDeploymentArchiveHandler archiveHandler = null;
		try {

			// if (deployer instanceof ActiveBPELDeployer) {
			archiveHandler = new ActiveBPELDeploymentArchiveHandler();
			// }
			archiveHandler.setArchiveFile(FilenameUtils.concat(pathToArchive, bprFile));
			prepareLoggingService(archiveHandler);
			executeInstrumentationOfBPEL(archiveHandler);
		} catch (JDOMException e) {
			throw new ArchiveFileException("", e);
		} catch (IOException e) {
			throw new ArchiveFileException("", e);
		}

		return archiveHandler.getArchiveFile().getName();
	}

	private static void executeInstrumentationOfBPEL(
			IDeploymentArchiveHandler archiveHandler) throws JDOMException,
			IOException, BpelException, BpelVersionException {
		IMetricHandler metricHandler = MetricHandler.getInstance();
		Statementmetric metric = (Statementmetric) metricHandler
				.addMetric(MetricHandler.STATEMENT_METRIC);
		metric.addAllBasicActivities();
		de.schlichtherle.io.File bpelFile;
		for (int i = 0; i < archiveHandler.getCountOfBPELFiles(); i++) {
			bpelFile = archiveHandler.getBPELFile(i);
			metricHandler.startInstrumentation(bpelFile);
		}
	}

	private static void prepareLoggingService(
			IDeploymentArchiveHandler archiveHandler) throws IOException,
			ArchiveFileException, JDOMException {
		// TODO configuration einlesen
		archiveHandler.addWSDLFile(new File(
				"C:/bpelunit/conf/_LogService_.wsdl"));
		prepareDeploymentDescriptor(archiveHandler);
	}

	private static void prepareDeploymentDescriptor(
			IDeploymentArchiveHandler archiveHandler)
			throws ArchiveFileException {
		de.schlichtherle.io.File descriptor;
		FileInputStream is = null;
		try {
			descriptor = archiveHandler.getDeploymentDescriptor();
			SAXBuilder builder = new SAXBuilder();
			is = new FileInputStream(descriptor);
			Document doc = builder.build(is);
			Element process = doc.getRootElement();
			addPartnerLink(process);
			addWSDLEntry(process);
			FileWriter writer = new FileWriter(descriptor);
			XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
			xmlOutputter.output(doc, writer);
			writer.close();
		} catch (ArchiveFileException e) {
			throw e;
		} catch (Exception e) {
			throw new ArchiveFileException("", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// <partnerLink name="PLT_LogService_">
		// <partnerRole endpointReference="static">
		// <wsa:EndpointReference
		// xmlns:s="http://www.bpelunit.org/coverage/logService"
		// xmlns:wsa="http://schemas.xmlsoap.org/ws/2003/03/addressing">
		// <wsa:Address>http://localhost:7777/ws/_LogService_</wsa:Address>
		// <wsa:ServiceName
		// PortName="_LogService_SOAP">s:_LogService_</wsa:ServiceName>
		// </wsa:EndpointReference>
		// </partnerRole>
		// </partnerLink>

		// <wsdl location="wsdl/_LogService_.wsdl"
		// namespace="http://www.bpelunit.org/coverage/logService"/>

	}

	private static void addWSDLEntry(Element process) {
		Element wsdl = new Element("wsdl", process.getNamespace());
		wsdl.setAttribute("location", "wsdl/_LogService_.wsdl");
		wsdl.setAttribute("namespace",
				"http://www.bpelunit.org/coverage/logService");
		Element references = process.getChild("references", process
				.getNamespace());
		references.addContent(wsdl);
	}

	private static void addPartnerLink(Element process) {
		Namespace ns = Namespace.getNamespace("wsa",
				"http://schemas.xmlsoap.org/ws/2003/03/addressing");
		process.addNamespaceDeclaration(ns);
		Element adress = new Element("Address", ns);
		adress.setText("http://localhost:7777/ws/_LogService_");
		Element serviceName = new Element("ServiceName", ns);
		serviceName.setAttribute("PortName", "_LogService_SOAP");
		serviceName.setText("s:_LogService_");

		Element endpointReference = new Element("EndpointReference", ns);
		endpointReference.addNamespaceDeclaration(Namespace.getNamespace("s",
				"http://www.bpelunit.org/coverage/logService"));
		endpointReference.addContent(adress);
		endpointReference.addContent(serviceName);
		Element partnerRole = new Element("partnerRole", process.getNamespace());
		partnerRole.setAttribute("endpointReference", "static");
		partnerRole.addContent(endpointReference);

		Element partnerLink = new Element("partnerLink", process.getNamespace());
		partnerLink.setAttribute("name", "PLT_LogService_");
		partnerLink.addContent(partnerRole);

		Element partnerLinks = process.getChild("partnerLinks", process
				.getNamespace());
		partnerLinks.addContent(partnerLink);
	}

}