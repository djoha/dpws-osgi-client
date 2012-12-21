package fi.tut.fast.dpws.device.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.eventing.SubscribeResponse;

import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.DpwsClient;
import fi.tut.fast.dpws.device.Operation;
import fi.tut.fast.dpws.utils.DPWSCommunication;
import fi.tut.fast.dpws.utils.DPWSMessageFactory;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;
import fi.tut.fast.dpws.utils.DPWSXmlUtil.TypeHandler;

public class OperationReference extends Operation{

    private static final transient Logger logger = Logger.getLogger(DpwsClient.class.getName());

	ServiceRef parentService;
	
	boolean event = false;
	
	public OperationReference(String name, QName portType ,  ServiceRef parentService, QName inputElement, QName outputElement, QName faultElement){

		hasInput = (inputElement != null);
		hasOutput = (outputElement != null);
		hasFault = (faultElement != null);
		
		this.inputElement = inputElement;
		this.outputElement = outputElement;
		this.faultElement = faultElement;
		this.parentService = parentService;
		this.name = name;
		this.portType = portType;

		event = ( inputElement == null );
		
		subscriptionIds = new ArrayList<String>();
		
		logger.info(String.format("%s added: %s", isEvent()? "Event" : "Operation", getName()));
	}
	
	public OperationReference(String name, QName portType , ServiceRef parentService, QName outputElement){
		 this( name,  portType, parentService, null, outputElement, null);
	}
	
	public OperationReference(String name, QName portType ,  ServiceRef parentService, QName inputElement, QName outputElement){
		 this( name, portType, parentService, inputElement, outputElement, null);
	}

	@Override
	public String subscribe(String listenerEndpoint) throws SOAPException, XMLStreamException{

		String refId = DPWSMessageFactory.newUUID();
		
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage msg = DPWSMessageFactory.getSubscribeMessage(getOutputAction(),
												parentService.getAddress().toString(),
												listenerEndpoint,
												refId);
		SOAPMessage resp = conn.call(msg, parentService.getAddress().toString());
		
		try {
			SubscribeResponse subResp = (SubscribeResponse)DPWSXmlUtil.getInstance().unmarshalSoapBody(resp);
			String refIdOut = subResp.getSubscriptionManager().getReferenceParameters().getIdentifier();
			logInfo("Subscribed.  Reference: " + refIdOut );
			subscriptionIds.add(refIdOut);
			return refIdOut;
		} catch (Exception e) {
			logError("Failed to unmarshal SubscriptionResponse",e);
		}

		return null;

	}

	@Override
	public void unsubscribe(String refId) throws SOAPException{
		
		subscriptionIds.remove(refId);
		
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage unsub = DPWSMessageFactory.getUnsubscribeMessage(getOutputAction(), parentService.getAddress().toString(), refId);
		SOAPMessage resp = conn.call(unsub, parentService.getAddress().toString());
 
		logInfo("Unsubscribed.  Reference: " + refId );
		
	}

	public void unsubscribeAll() throws SOAPException{
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		for(String refId : subscriptionIds){
			SOAPMessage unsub = DPWSMessageFactory.getUnsubscribeMessage(getOutputAction(), parentService.getAddress().toString(), refId);
			SOAPMessage resp = conn.call(unsub, parentService.getAddress().toString());
		}
		logInfo("Unsibscribed from the following: " + Arrays.toString(subscriptionIds.toArray(new String[]{})));
		subscriptionIds.clear();
	}

	@Override
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

	@Override
	public List<SchemaType> getTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
