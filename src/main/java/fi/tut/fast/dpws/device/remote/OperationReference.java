package fi.tut.fast.dpws.device.remote;

import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

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
	String portType;
	
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
	
	
	public XmlObject invoke(XmlObject input) throws SOAPException, XmlException, ParserConfigurationException{
		
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage msg = DPWSMessageFactory.getInputMessage(getInputAction(), parentService.getAddress().toString());
		
		if(!parentService.getTypeHandler().validate(input)){
//			throw new XmlException("Invalid Input Message. Check Logs.");
		}
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		msg.getSOAPBody().removeContents();
		Node node = input.getDomNode().cloneNode(true);
		msg.getSOAPPart().importNode(node, true);
		msg.getSOAPBody().appendChild(node);
		
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
					namespace,
					portType,
					name,
					DPWSConstants.INPUT_ACTION_SUFFIX);
	}
	
	public String getOutputAction(){
		if(outputAction != null){
			return outputAction;
		}
		return String.format("%s/%s/%s%s",
					namespace,
					portType,
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
	
	
}
