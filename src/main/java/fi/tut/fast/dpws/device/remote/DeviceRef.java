package fi.tut.fast.dpws.device.remote;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.xmlbeans.XmlException;
import org.xmlsoap.schemas.devprof.DeviceMetadataDialectURIs;
import org.xmlsoap.schemas.devprof.DeviceRelationshipTypeURIs;
import org.xmlsoap.schemas.devprof.HostServiceType;
import org.xmlsoap.schemas.devprof.LocalizedStringType;
import org.xmlsoap.schemas.devprof.Relationship;
import org.xmlsoap.schemas.devprof.ThisDeviceType;
import org.xmlsoap.schemas.devprof.ThisModelType;
import org.xmlsoap.schemas.discovery.ByeType;
import org.xmlsoap.schemas.discovery.HelloType;
import org.xmlsoap.schemas.discovery.ProbeMatchType;
import org.xmlsoap.schemas.discovery.ProbeMatchesType;
import org.xmlsoap.schemas.discovery.ProbeType;
import org.xmlsoap.schemas.discovery.ScopesType;
import org.xmlsoap.schemas.mex.Metadata;
import org.xmlsoap.schemas.mex.MetadataSection;

import fi.tut.fast.dpws.DpwsClient;
import fi.tut.fast.dpws.device.Device;
import fi.tut.fast.dpws.utils.DPWSCommunication;
import fi.tut.fast.dpws.utils.DPWSMessageFactory;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;

public class DeviceRef extends Device{

    private static final transient Logger logger = Logger.getLogger(DpwsClient.class.getName());

	private SOAPConnection conn;
	
//	<wsdp:Manufacturer xml:lang="en">Inico
//			Technologies
//			Ltd.</wsdp:Manufacturer>
//		<wsdp:ManufacturerUrl>http://www.inicotech.com/</wsdp:ManufacturerUrl>
//		<wsdp:ModelName xml:lang="en">S1000</wsdp:ModelName>
//	</wsdp:ThisModel>
//</wsx:MetadataSection>
//<wsx:MetadataSection
//	Dialect="http://schemas.xmlsoap.org/ws/2006/02/devprof/ThisDevice">
//	<wsdp:ThisDevice>
//		<wsdp:FriendlyName xml:lang="en">SomeDevice</wsdp:FriendlyName>
//		<wsdp:FirmwareVersion>2.3.3RC2</wsdp:FirmwareVersion>
//		<wsdp:SerialNumber>2-62</wsdp:SerialNumber>
//	
	
	public Status status = Status.UNKNOWN;

	protected DeviceRef() {

	}
	
	public static DeviceRef fromHello(HelloType hello){
		return new DeviceRef(hello);
	}
	
	public static DeviceRef fromProbeMatch(ProbeMatchType pm){
		return new DeviceRef(pm);
	}

	private DeviceRef(HelloType hello) {
		id = hello.getEndpointReference().getAddress().getValue();
		metadataVersion = hello.getMetadataVersion();
		xAddrs = hello.getXAddrs();
		types = hello.getTypes();
		status = Status.DISCOVERED;
		scopes = hello.getScopes();
		init();
		updateRemoteDeviceMetadata();
	}

	private DeviceRef(ProbeMatchType pm) {
		id = pm.getEndpointReference().getAddress().getValue();
		metadataVersion = pm.getMetadataVersion();
		xAddrs = pm.getXAddrs();
		types = pm.getTypes();
		status = Status.DISCOVERED;
		
		init();
		updateRemoteDeviceMetadata();
	}
	
	private void init(){
		try {
			conn = DPWSCommunication.getNewSoapConnection();
		} catch (SOAPException e) {
			System.out.println("Error getting SOAPConnection.");
			e.printStackTrace();
		}
	}
	
	public void updateRemoteDeviceMetadata(){
		try {
			SOAPMessage metadataMsg = conn.call(DPWSMessageFactory.createGetRequest(getXAddress()), getXAddress());
			Metadata metadata = (Metadata) DPWSXmlUtil.getInstance().unmarshalSoapBody(metadataMsg);
			setDeviceMetadata(metadata);
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reportBye(){
		status = Status.BYE_RECIEVED;
	}
	
	public Status getStatus(){
		return status;
	}

	public URI getId() {
		return id;
	}

	public String getXAddress() {
		return xAddrs.get(0);
	}

	public List<String> getXAddresses() {
		return xAddrs;
	}

	public long getMetadataVersion() {
		return metadataVersion;
	}

	public QName getType() {
		if (types == null) {
			return null;
		}
		if (types.isEmpty()) {
			return null;
		}
		return types.get(0);
	}
	
	public enum Status {
	    DISCOVERED, FULLY_RESOLVED, ACTIVE, UNKNOWN, DEAD, BYE_RECIEVED
	}
	
	@Override
	public String toString(){
		StringWriter writer = new StringWriter();
		try {
			DPWSXmlUtil.getInstance().marshall(getDeviceMetadata(), writer);
		} catch (JAXBException e) {
			e.printStackTrace();
			return("Device Metadata Flawed.");
		}
		return writer.toString();
	}
	
	
	protected void setDeviceMetadata(Metadata metadata) throws JAXBException{
		
		DPWSXmlUtil util = DPWSXmlUtil.getInstance();
		logInfo("Setting Device Metadata.");
		
		for(MetadataSection section : metadata.getMetadataSection()){
			switch(DeviceMetadataDialectURIs.fromValue(section.getDialect())){
				case ThisDevice:
					logInfo("Parsing ThisDevice");
					ThisDeviceType dt = (ThisDeviceType)util.unwrap(section.getAny());
					setFirmwareVersion(dt.getFirmwareVersion());
					setSerialNumber(dt.getSerialNumber());
					for(LocalizedStringType fm : dt.getFriendlyName()){
						addFriendlyName(fm.getLocale(),fm.getValue());
					}
					break;
				case ThisModel:
					logInfo("Parsing ThisModel");
					ThisModelType mt = (ThisModelType)util.unwrap(section.getAny());;
					for(LocalizedStringType lst : mt.getManufacturer()){
						addManufacturer(lst.getLocale(),lst.getValue());
					}
					for(LocalizedStringType lst : mt.getModelName()){
						addModelName(lst.getLocale(),lst.getValue());
					}
					setModelNumber(mt.getModelNumber());
					setManufacturerUrl(mt.getManufacturerUrl());
					setModelUrl(mt.getModelUrl());
					setPresentationUrl(mt.getPresentationUrl());
					break;
				case Relationship:
					Relationship rel = (Relationship)util.unwrap(section.getAny());
					for(Object obj : rel.getAny()){
						switch(DeviceRelationshipTypeURIs.fromValue(rel.getType())){
							case host:
								HostServiceType host = (HostServiceType)util.unwrap(obj);
								try {
									ServiceRef service = new ServiceRef(host);
									hostedServices.put(service.getServiceId(),service);
									logInfo("Added hosted service " + service.getServiceId());
								} catch (SOAPException e) {
									logError("Error adding hosted service " +
											host.getEndpointReference().get(0).getAddress().getValue().toString()
											,e);
								} catch (IOException e) {
									logError("Error adding hosted service " +
											host.getEndpointReference().get(0).getAddress().getValue().toString()
											,e);
								} catch (URISyntaxException e) {
									logError("Error adding hosted service " +
											host.getEndpointReference().get(0).getAddress().getValue().toString()
											,e);
								} catch (XmlException e) {
									logError("Error adding hosted service " +
											host.getEndpointReference().get(0).getAddress().getValue().toString()
											,e);
								}
								break;
							default:
								// Unknown relationship type
						}
					}
					break;
				default:
					//nope.
			}
		}
	}
	
	
	private void logError(String msg, Throwable err){
		logger.log(Level.SEVERE,String.format(" [%s (%s)] : %s",
				(getFriendlyName() == null ? "New Device" : getFriendlyName())
				, getId().toString(),  msg),err);
	}
	
	private void logInfo(String msg){
		logger.info(String.format(" [%s (%s)] : %s",
				(getFriendlyName() == null ? "New Device" : getFriendlyName())
				, getId().toString(),  msg));
	}
	
	
	public ServiceRef getService(String serviceId){
		return (ServiceRef)hostedServices.get(serviceId);
	}
	
	public Collection<ServiceRef> getServices(){
		return (Collection)hostedServices.values();
	}
	
	public List<SubscriptionRef> subscribe(String filter, String notifyTo){
		if(filter == null ){
			return Collections.EMPTY_LIST;
		}
		List<SubscriptionRef> subs = new ArrayList<SubscriptionRef>();
		for(ServiceRef s : getServices()){
			subs.add(s.subscribe(filter, notifyTo));
		}
		return subs;
	}
	
}
