package fi.tut.fast.dpws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.BundleContext;
import org.xmlsoap.schemas.discovery.ByeType;
import org.xmlsoap.schemas.discovery.HelloType;
import org.xmlsoap.schemas.discovery.ProbeMatchesType;

import fi.tut.fast.dpws.device.remote.DeviceRef;
import fi.tut.fast.dpws.device.remote.OperationReference;
import fi.tut.fast.dpws.device.remote.ServiceRef;
import fi.tut.fast.dpws.device.remote.SubscriptionRef;
import fi.tut.fast.dpws.utils.DPWSMessageFactory;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;
import fi.tut.fast.dpws.utils.DeviceRegistry;
import fi.tut.fast.dpws.utils.SOAPUtil;


public class DpwsClient implements IDpwsClient{

    private static final transient Logger logger = Logger.getLogger(DpwsClient.class.getName());
	private String someProperty = "thing";
	private String probeMatchEndpointAddress;
	private BundleContext context;
	private String defaultEventSink;
	private String eventTypeFilter;
	private String eventSinkAddress;
	
	
	private Map<String,SubscriptionRef> subscriptions;
	
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


	public String getDefaultEventSink() {
		return defaultEventSink;
	}


	public void setDefaultEventSink(String defaultEventSink) {
		this.defaultEventSink = defaultEventSink;
	}


	public String getEventTypeFilter() {
		return eventTypeFilter;
	}


	public void setEventTypeFilter(String eventTypeFilter) {
		if(eventTypeFilter.equals("[[[NULL_FILTER]]]")){
			this.eventTypeFilter = null;
		}else{
			this.eventTypeFilter = eventTypeFilter;
		}
	}


	public void destroy() throws Exception {
    	logger.info("OSGi Bundle Stopping.");
    	for(SubscriptionRef ref : subscriptions.values()){
    		ref.unsubscribe();
    		logger.info("Unsubscribed: " + ref);
    	}
	}

	public void init() throws Exception {
    	logger.info("OSGi Bundle Initialized.");
    	DPWSMessageFactory.init();
    	DPWSXmlUtil.init(context);
    	subscriptions = new HashMap<String,SubscriptionRef>();
    	registry = new DeviceRegistry();
    	dpwsScan();
	}
	
	private void handleNewDeviceRefs(List<DeviceRef> devs){
		for(DeviceRef ref :devs){
			handleNewDeviceRef(ref);
		}
	}
	
	private String getNotifyTo(){

		if(eventSinkAddress != null){
			return (eventSinkAddress.isEmpty()? defaultEventSink : eventSinkAddress);
		}
		return defaultEventSink;
	}
	
	private void handleNewDeviceRef(DeviceRef ref){
		List<SubscriptionRef> subList = ref.subscribe(eventTypeFilter,getNotifyTo());
		addSubscriptions(subList);
		logger.info("Added Subscriptions: " + Arrays.toString(subList.toArray()));
	}
	
	private void addSubscriptions(List<SubscriptionRef> newSubs){
		for(SubscriptionRef ref: newSubs){
			subscriptions.put(ref.getId(), ref);
		}
	}
	
	public void eventReceived(Exchange message) throws IOException, SOAPException{
		SOAPMessage event = DPWSMessageFactory.recieveMessage(message.getIn().getBody(InputStream.class));
		
		System.out.format("Event received: %s [%s]\n", SOAPUtil.getActionHeader(event)
													, SOAPUtil.getWseIdentifierHeader(event));
//		event.writeTo(System.out);
	}

	public void helloReceived(Exchange message) throws Exception{
		HelloType hello = (HelloType) DPWSXmlUtil.getInstance().unmarshalSoapBody(message);
		logger.info("Received Hello from " + hello.getEndpointReference().getAddress().getValue());
		
		handleNewDeviceRef(registry.registerDevice(hello));
	}

	public void byeReceived(Exchange message) throws Exception{
		ByeType bye = (ByeType) DPWSXmlUtil.getInstance().unmarshalSoapBody(message);
		logger.info("Received Bye from " + bye.getEndpointReference().getAddress().getValue());
		registry.reportBye(bye);
	}

	public void probeReceived(Exchange message) throws SOAPException, IOException, JAXBException{
		logger.info("Probe Message recieved. Ignoring....");
		System.out.println("Probe Message recieved. Ignoring....");
	}
	
	public void probeMatchesReceived(Exchange message) throws Exception{
		ProbeMatchesType matches = (ProbeMatchesType) DPWSXmlUtil.getInstance().unmarshalSoapBody(message);
		logger.info("Received Probe Matches from " + matches.getProbeMatch().get(0).getEndpointReference().getAddress().getValue());
		System.out.println("Received Probe Matches from " + matches.getProbeMatch().get(0).getEndpointReference().getAddress().getValue());
		
		handleNewDeviceRefs(registry.registerDevice(matches));
		
	}
	
//	public void messageReceived(Exchange message) throws SOAPException, IOException{
//		System.out.println("Unknown Message Received:");
//		SOAPMessage msg = DPWSMessageFactory.recieveMessage(message.getIn().getBody(InputStream.class));
//		msg.writeTo(System.out);
//		System.out.flush();
//		System.out.println("");
//	}

	@Override
	public void dpwsScan() {
		
    	try {
        	ByteArrayOutputStream probe = new ByteArrayOutputStream(DPWSConstants.WSD_PROBE_MAX_SIZE);
			SOAPMessage env = DPWSMessageFactory.getDiscoveryProbe();
			env.getSOAPBody().addBodyElement(DPWSConstants.WSD_PROBE_ELEMENT_QNAME);
			env.writeTo(probe);
			
			System.out.println("Client Sending probe: ");
			env.writeTo(System.out);
			System. out.println("\n");

	    	p.sendProbe(probe.toByteArray());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to send Probe.", e);
		}
	}


	@Override
	public Map<String, DeviceRef> getDiscoveredDevices() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, ServiceRef> listServices(String deviceId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, OperationReference> listOperations(String serviceId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public XmlObject getInputXmlTemplate(String operationid) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public XmlObject invokeOperation(String operationId, XmlObject input) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
