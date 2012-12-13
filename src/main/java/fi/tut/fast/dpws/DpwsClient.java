package fi.tut.fast.dpws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.osgi.framework.BundleContext;
import org.xmlsoap.schemas.discovery.ByeType;
import org.xmlsoap.schemas.discovery.HelloType;
import org.xmlsoap.schemas.discovery.ProbeMatchesType;

import fi.tut.fast.dpws.utils.DPWSMessageFactory;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;
import fi.tut.fast.dpws.utils.DeviceRegistry;


public class DpwsClient implements IDpwsClient{

    private static final transient Logger logger = Logger.getLogger(DpwsClient.class.getName());
	private String someProperty = "thing";
	private String probeMatchEndpointAddress;
	private BundleContext context;
	
	@Produce(uri="direct:discoveryProbe")
	Prober p;
	
	private DeviceRegistry registry;
	
	interface Prober{
		public void sendProbe(byte[] msg);
	}
	
	
	public String getSomeProperty() {
		return someProperty;
	}


	public void setSomeProperty(String someProperty) {
		this.someProperty = someProperty;
	}


	public String getProbeMatchEndpointAddress() {
		return probeMatchEndpointAddress;
	}


	public void setProbeMatchEndpointAddress(String probeMatchEndpointAddress) {
		this.probeMatchEndpointAddress = probeMatchEndpointAddress;
	}


	public BundleContext getContext() {
		return context;
	}


	public void setContext(BundleContext context) {
		this.context = context;
	}


	@Override
	public String sayHello(Object whatever){
		  logger.info(">>>> " + someProperty + " " + whatever);
		  return "hello " + whatever;
	}


	public void destroy() throws Exception {
    	logger.info("OSGi Bundle Stopping.");
	}

	public void init() throws Exception {
    	logger.info("OSGi Bundle Initialized.");
    	DPWSMessageFactory.init();
    	DPWSXmlUtil.init(context);
    	registry = new DeviceRegistry();
    	
    	ByteArrayOutputStream probe = new ByteArrayOutputStream(DPWSConstants.WSD_PROBE_MAX_SIZE);
    	DPWSMessageFactory.getDiscoveryProbe(probeMatchEndpointAddress).writeTo(probe);
    	p.sendProbe(probe.toByteArray());
    	
	}

	public void helloReceived(Exchange message) throws Exception{
		HelloType hello = (HelloType) DPWSXmlUtil.getInstance().unmarshalSoapBody(message);
		logger.info("Received Hello from " + hello.getEndpointReference().getAddress().getValue());
		registry.registerDevice(hello);
	}

	public void byeReceived(Exchange message) throws Exception{
		ByeType bye = (ByeType) DPWSXmlUtil.getInstance().unmarshalSoapBody(message);
		logger.info("Received Bye from " + bye.getEndpointReference().getAddress().getValue());
		registry.reportBye(bye);
	}

	public void probeReceived(Exchange message) throws SOAPException, IOException, JAXBException{
		logger.info("Probe Message recieved. Ignoring....");
	}
	
	public void probeMatchesReceived(Exchange message) throws Exception{
		ProbeMatchesType matches = (ProbeMatchesType) DPWSXmlUtil.getInstance().unmarshalSoapBody(message);
		logger.info("Received Probe Matches from " + matches.getProbeMatch().get(0).getEndpointReference().getAddress().getValue());
		registry.registerDevice(matches);
	}
	
	
	public void messageReceived(Exchange message) throws SOAPException, IOException{
		System.out.println("Some Message:");
		SOAPMessage msg = DPWSMessageFactory.recieveMessage(message.getIn().getBody(InputStream.class));
		msg.writeTo(System.out);
		System.out.flush();
	}
	
	
//	public void stripHeader(Exchange message) throws IOException, SOAPException{
//		SOAPMessage msg = DPWSMessageFactory.recieveMessage(message.getIn().getBody(InputStream.class));
//	
//		
//		msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "false");
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		msg.saveChanges();
//		msg.writeTo(baos);
//		
//		System.out.println("Declaration stripped..." );
//		msg.writeTo(System.out);
//		
//		message.getOut().setBody(baos.toByteArray());
//	}

}
