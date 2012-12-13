package fi.tut.fast.dpws.device;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.xmlsoap.schemas.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.devprof.DeviceMetadataDialectURIs;
import org.xmlsoap.schemas.devprof.HostServiceType;
import org.xmlsoap.schemas.mex.Metadata;
import org.xmlsoap.schemas.mex.MetadataSection;

import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.utils.DPWSTypeManager;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;

public class Service {

	
	protected URI namespace;
	protected URI serviceId;
	private List<URI> eprs = new ArrayList<URI>();
	private List<QName> portTypes = new ArrayList<QName>();
	static org.xmlsoap.schemas.mex.ObjectFactory mexObjFactory;
	static org.xmlsoap.schemas.devprof.ObjectFactory defProfFactory;
	private DPWSTypeManager types;
	
	protected Service(){
		
	}
	
	protected Service(URI serviceId){
		this.serviceId = serviceId;
	}
	
	public Service(String namespace, String serviceId) throws URISyntaxException{
		this.namespace = new URI(namespace);
		this.serviceId = new URI(serviceId);
		this.types = DPWSTypeManager.getInstance(namespace);
	}
	
	public String getServiceId(){
		return serviceId.toString();
	}
	
	public String getWSDLFileLocation(){
		return "someplace?wsdl";
	}
	
	public void addEndpointReference(String url) throws URISyntaxException, MalformedURLException{
		URL junk = new URL(url);  // Check if valid URL
		eprs.add(new URI(url));
	}
	
	public List<URI> getEndpointReferences(){
		return eprs;
	}
	
	public List<QName> getPortTypes(){
		return portTypes;
	}
	
	public void addPortType(String name){
		portTypes.add(new QName(namespace.toString(),name));
	}
	
	protected void setHostedMetadata(HostServiceType hosted){
		serviceId = hosted.getServiceId();
		for(EndpointReferenceType  epr : hosted.getEndpointReference()){
			eprs.add(epr.getAddress().getValue());
		}
		for(QName portType : hosted.getTypes()){
			portTypes.add(portType);
		}
	}
	
	public JAXBElement<HostServiceType> getHostedMetadata() throws JAXBException{

    	HostServiceType host = new HostServiceType();
    	host.setServiceId(serviceId);
    	for(URI u : eprs){
    		host.addEndpointReference(new EndpointReferenceType(u));
    	}
    	for(QName portType : portTypes){
    		host.addType(portType);
    	}
    	return DPWSXmlUtil.getInstance().createHosted(host);

	}
	
	public Metadata getMetadata(){
		
		if(mexObjFactory == null){
			mexObjFactory = new org.xmlsoap.schemas.mex.ObjectFactory();
		}
		
    	MetadataSection wsdlSection = mexObjFactory.createMetadataSection();
    	wsdlSection.setDialect(DPWSConstants.METADATA_SECTION_DIALECT_WSDL);
    	wsdlSection.setAny(mexObjFactory.createLocation(getWSDLFileLocation()));
		
    	Metadata metadata = mexObjFactory.createMetadata();
    	metadata.addMetadataSection(wsdlSection);
		
    	return metadata;
	}
	
}
