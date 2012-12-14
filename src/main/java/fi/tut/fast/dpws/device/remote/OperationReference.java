package fi.tut.fast.dpws.device.remote;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.eventing.SubscribeResponse;

import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.DpwsClient;
import fi.tut.fast.dpws.utils.DPWSCommunication;
import fi.tut.fast.dpws.utils.DPWSMessageFactory;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;
import fi.tut.fast.dpws.utils.DPWSXmlUtil.TypeHandler;

public class OperationReference {

    private static final transient Logger logger = Logger.getLogger(DpwsClient.class.getName());
	
	String name;
	String namespace;
	ServiceRef parentService;
	String inputAction;
	String outputAction;
	QName portType;
	
	QName inputElement;
	QName outputElement;
	QName faultElement;
	
	boolean event = false;
	
	public OperationReference(String name, QName portType ,  ServiceRef parentService, QName inputElement, QName outputElement, QName faultElement){

		this.inputElement = inputElement;
		this.outputElement = outputElement;
		this.faultElement = faultElement;
		this.parentService = parentService;
		this.name = name;
		this.portType = portType;

		event = ( inputElement == null );
		
		logger.info(String.format("%s added: %s", isEvent()? "Event" : "Operation", getName()));
	}
	
	public OperationReference(String name, QName portType , ServiceRef parentService, QName outputElement){
		 this( name,  portType, parentService, null, outputElement, null);
	}
	
	public OperationReference(String name, QName portType ,  ServiceRef parentService, QName inputElement, QName outputElement){
		 this( name, portType, parentService, inputElement, outputElement, null);
	}

	public XmlObject getInputParamter(){
		return parentService.getTypeHandler().getElementTemplate(inputElement);
	}

	public XmlObject getOutputParameter(){
		return parentService.getTypeHandler().getElementTemplate(outputElement);
	}

	public XmlObject getFaultParamter(){
		return parentService.getTypeHandler().getElementTemplate(faultElement);
	}

	public XmlObject getInputParamter(String value){
		return parentService.getTypeHandler().populateElement(inputElement, value);
	}

	public XmlObject getOutputParameter(String value){
		return parentService.getTypeHandler().populateElement(outputElement, value);
	}

	public XmlObject getFaultParamter(String value){
		return parentService.getTypeHandler().populateElement(faultElement, value);
	}
	
	public XmlObject getInputParamter(Map<String,String> values){
		return parentService.getTypeHandler().populateElement(inputElement, values);
	}

	public XmlObject getOutputParameter(Map<String,String> values){
		return parentService.getTypeHandler().populateElement(outputElement, values);
	}

	public XmlObject getFaultParamter(Map<String,String> values){
		return parentService.getTypeHandler().populateElement(faultElement, values);
	}
	
	public String subscribe(String listenerEndpoint) throws SOAPException, XPathExpressionException, XMLStreamException{

		String refId = DPWSMessageFactory.newUUID();
		
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage msg = DPWSMessageFactory.getSubscribeMessage(getOutputAction(),
												parentService.getAddress().toString(),
												listenerEndpoint,
												refId);
		SOAPMessage resp = conn.call(msg, parentService.getAddress().toString());
		
		try {
			SubscribeResponse subResp = (SubscribeResponse)DPWSXmlUtil.getInstance().unmarshalSoapBody(resp);
		} catch (Exception e) {
			logError("Failed to unmarshal SubscriptionResponse",e);
		}
		
		String refIdOut = DPWSXmlUtil.extractReferenceParam(resp);
		
		logInfo("Subscribed.  Reference: " + refIdOut );
		
		return refIdOut;
		
	}
	
	public XmlObject invoke(XmlObject input) throws SOAPException, XmlException, ParserConfigurationException, SAXException, IOException{
		
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage msg = DPWSMessageFactory.getInputMessage(getInputAction(), parentService.getAddress().toString());
		
		if(!parentService.getTypeHandler().validate(input)){
			throw new XmlException("Validation failed. Check Logs.");
		}
				
		Node n = msg.getSOAPPart().importNode(input.getDomNode(),true);
		msg.getSOAPBody().appendChild(n);

		logInfo("Invoked.");
		
		SOAPMessage outMessage = conn.call(msg, parentService.getAddress().toString());
		return parentService.getTypeHandler().unmarshal(outMessage.getSOAPBody().extractContentAsDocument(),outputElement);
		
	}
	
	//
	
	public String getName(){
		return name;
	}
	
	public String getInputAction(){
		if(isEvent()){
			return null;
		}
		if(inputAction != null){
			return inputAction;
		}
		return String.format("%s/%s/%s%s",
					portType.getNamespaceURI(),
					portType.getLocalPart(),
					name,
					DPWSConstants.INPUT_ACTION_SUFFIX);
	}
	
	public String getOutputAction(){
		if(outputAction != null){
			return outputAction;
		}
		return String.format("%s/%s/%s%s",
				portType.getNamespaceURI(),
				portType.getLocalPart(),
				name,
				isEvent() ? "" : DPWSConstants.OUTPUT_ACTION_SUFFIX);
	}
	
	public boolean isEvent(){
		return event;
	}
	
	public void setInputAction(String inputAction){
		this.inputAction = inputAction;
	}
	
	public void setOutputAction(String outputAction){
		this.setOutputAction(outputAction);
	}
	
	private void logInfo(String msg){
		logger.info(String.format("[Operation %s] - %s", getName(), msg));
	}
	
	private void logError(String msg, Throwable e){
		logger.log(Level.SEVERE,String.format("[Operation %s] - %s", getName(), msg),e);
	}
	
}
