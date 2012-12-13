package fi.tut.fast.dpws.device.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.xmlbeans.XmlException;
import org.eclipse.jetty.util.log.Log;
import org.xmlsoap.schemas.devprof.HostServiceType;
import org.xmlsoap.schemas.mex.Metadata;
import org.xmlsoap.schemas.mex.MetadataSection;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.TTypes;
import org.xmlsoap.schemas.wsdl.TOperation.OperationParameter;
import org.xmlsoap.schemas.wsdl.TPortType;

import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.DpwsClient;
import fi.tut.fast.dpws.device.Service;
import fi.tut.fast.dpws.utils.DPWSCommunication;
import fi.tut.fast.dpws.utils.DPWSMessageFactory;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;
import fi.tut.fast.dpws.utils.DPWSXmlUtil.TypeHandler;

public class ServiceRef extends Service {

    private static final transient Logger logger = Logger.getLogger(DpwsClient.class.getName());
	private URL wsdlLocation;
	private TypeHandler handler;
	private Map<String,OperationReference> operations;
	
	public ServiceRef(String namespace, String serviceId)
			throws URISyntaxException {
		super(namespace, serviceId);
		// TODO Auto-generated constructor stub
	}

	public ServiceRef(HostServiceType data) throws SOAPException, JAXBException, IOException, URISyntaxException, XmlException{
		super(data.getServiceId());
		logInfo("Service " + data.getServiceId().toString() + " Added.");
		setHostedMetadata(data);
		sendGet();
	}
	
	public URI getAddress(){
		return getEndpointReferences().get(0);
	}
	
	public void sendGet() throws SOAPException, JAXBException, IOException, URISyntaxException, XmlException{
		
		logger.info("Sending GET Request to Service " + getServiceId());
		
		SOAPMessage request = DPWSMessageFactory.createGetRequest(getAddress().toString());
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage response = conn.call(request, this.getAddress().toString());
		Metadata data = (Metadata)DPWSXmlUtil.getInstance().unmarshalSoapBody(response);
		for(MetadataSection section : data.getMetadataSection()){
			if(section.getDialect().equals(DPWSConstants.METADATA_SECTION_DIALECT_WSDL)){
				handleWSDLMetadataSection(section);
			}else{
				logger.log(Level.SEVERE, "Dialect \"" + section.getDialect() + "\" not recognized. Ignoring.");
			}
		}
	}
	
	private void handleWSDLMetadataSection(MetadataSection section) throws JAXBException, URISyntaxException, IOException, XmlException{

		logInfo("Handling WSDL Metadata Section");
		String loc = section.getLocation();
		if(loc != null){
			wsdlLocation = new URL(section.getLocation());
			TDefinitions wsdl = fetchRemoteWSDL();
			parseWSDL(wsdl);
			

			
//			List<Object> modelInfo = new ArrayList<Object>();
//			
//			for(TTypes types : wsdl.getTypes()){
//				
//				for(Object o : types.getAny()){
//					modelInfo.add(o);
//				}
////				JAXBElement<TTypes> el = new JAXBElement<TTypes>(DPWSConstants.WSDL_TYPES_QNAME,TTypes.class, types);
////				DPWSXmlUtil.getInstance().marshall(el, System.out);
//				
//			}
//			
			
			
			
				
		}
	}
	
	private TDefinitions fetchRemoteWSDL() {

		try {
			HttpURLConnection conn = (HttpURLConnection) wsdlLocation.openConnection();
			conn.setRequestMethod("GET");
//			conn.setDoOutput(true);
			conn.setDoInput(true);
//			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
//			out.writeBytes("reboot=reboot&__end=");
//			out.flush();
//			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			TDefinitions wsdl = DPWSXmlUtil.getInstance().unmarshalWSDL(in);
			conn.disconnect();
			return wsdl;
		} catch (ProtocolException e) {
			logError("Error fetching WSDL:",e);
		} catch (IOException e) {
			logError("Error fetching WSDL:",e);
		} catch (JAXBException e) {
			logError("Error fetching WSDL:",e);
		}
		
		return null;


	}

	private void parseWSDL(TDefinitions wsdl) throws URISyntaxException, IOException, JAXBException, XmlException {
		
		
		namespace = new URI(wsdl.getTargetNamespace());

		logInfo("Parsing WSDL targetNamespace=\"%s\"",wsdl.getTargetNamespace());
		logInfo("Compiling Types.");
		
		handler = DPWSXmlUtil.getInstance().compileTypeSchemas(getServiceId(), wsdl.getTypes());
		
		operations = new HashMap<String,OperationReference>();
		
		for(TPortType pt : wsdl.getPortTypes()){
			QName portType = new QName(namespace.toString(),pt.getName());
			for(TOperation op : pt.getOperation()){
				
				QName input = null;
				QName output = null;
				QName fault = null;

				QName msg = op.getMessageQName(OperationParameter.input);
				if(msg != null){
					 input = wsdl.getMessage(msg).getPart().get(0).getElement();
				}
				msg = op.getMessageQName(OperationParameter.output);
				if(msg != null){
					output =  wsdl.getMessage(msg).getPart().get(0).getElement();
				}
				msg = op.getMessageQName(OperationParameter.fault);
				if(msg != null){
					fault =  wsdl.getMessage(msg).getPart().get(0).getElement();
				}
				

				
				OperationReference newOp = new OperationReference(op.getName(),
									portType,
									this,
									input,output,fault);
				
				operations.put(op.getName(), newOp);
				logInfo("\nOperation %s:\n\tinput: %s\n\toutput: %s\n\tfault: %s\n",
						op.getName(), 
						input == null ? "[]" : input.toString(),
						output == null ? "[]" : output.toString(),
						fault == null ? "[]" : fault.toString());
			}
		}
		
	}
	
	
	public TypeHandler getTypeHandler(){
		return handler;
	}
	
	
	private void loadTypes(TTypes types){
		types.getAny();
		
//		URL bindingUrl = context.getBundle().getResource("/types/binding.xjb");
//    	InputSource bind = new InputSource(ResourceFactory.newUrlResource(bindingUrl).getInputStream());
//    	bind.setSystemId(JAXBSYSTEM_ID);
    	
//    	XJC xjc = new XJC();
//    	SchemaCompiler compiler = xjc.createSchemaCompiler();
//
//    	Options xjcOpts = compiler.getOptions();
//		xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );
//		xjcOpts.compatibilityMode = Options.EXTENSION;
//		xjcOpts.addBindFile(bind);
//		
//		return xjcOpts;
	}
	
	
	private void logInfo(String msg, String... args){
		Log.info(String.format(msg, args));
	}
	
	private void logError(String msg, Throwable err){
		logger.log(Level.SEVERE,String.format(" [%s ] : %s",
				(getServiceId() == null ? "New Service" : getServiceId())
				,  msg),err);
	}
	
	private void logInfo(String msg){
		logger.info(String.format(" [%s ] : %s",
				(getServiceId() == null ? "New Service" : getServiceId())
				,  msg));
	}

	public OperationReference getOperation(String name){
		return operations.get(name);
	}
	public Collection<OperationReference> getOperations(){
		List<OperationReference> events = new ArrayList<OperationReference>();
		for(OperationReference ref : operations.values()){
			if(!ref.isEvent()){
				events.add(ref);
			}
		}
		return events;
	}
	
	public List<OperationReference> getEvents(){
		List<OperationReference> events = new ArrayList<OperationReference>();
		for(OperationReference ref : operations.values()){
			if(ref.isEvent()){
				events.add(ref);
			}
		}
		return events;
	}
}
