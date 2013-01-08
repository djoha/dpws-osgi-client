package fi.tut.fast.dpws.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.xml.sax.SAXException;
import org.xmlsoap.schemas.wsdl.TBindingOperation;
import org.xmlsoap.schemas.wsdl.TBindingOperationFault;
import org.xmlsoap.schemas.wsdl.TBindingOperationMessage;
import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;
import org.xmlsoap.schemas.wsdl.soap12.TBody;
import org.xmlsoap.schemas.wsdl.soap12.TStyleChoice;
import org.xmlsoap.schemas.wsdl.soap12.UseChoice;

import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.DpwsClient;
import fi.tut.fast.dpws.device.remote.ServiceRef;

public abstract class Operation {

    private static final transient Logger logger = Logger.getLogger(DpwsClient.class.getName());
	
	protected String name;
	protected String namespace;
	protected ServiceRef parentService;
	protected String inputAction;
	protected String outputAction;
	protected QName portType;
	
	protected QName inputElement;
	protected QName outputElement;
	protected QName faultElement;
	
	protected boolean hasInput = false;
	protected boolean hasOutput = false;
	protected boolean hasFault = false;
	
	private List<String> subscriptionIds;
	
	
	boolean event = false;
	
	public Operation(){
		
	}
	
	public Operation(String name, QName portType ,  ServiceRef parentService, QName inputElement, QName outputElement, QName faultElement){

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
	
	public Operation(String name, QName portType , ServiceRef parentService, QName outputElement){
		 this( name,  portType, parentService, null, outputElement, null);
	}
	
	public Operation(String name, QName portType ,  ServiceRef parentService, QName inputElement, QName outputElement){
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
	
//	public abstract SubscriptionRef subscribe(String listenerEndpoint) throws SOAPException, XMLStreamException;
//	
//	public abstract void unsubscribe(String refId) throws SOAPException;
//	
//	public abstract XmlObject invoke(XmlObject input) throws SOAPException, XmlException, ParserConfigurationException, SAXException, IOException;
	
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
		inputAction = String.format("%s/%s/%s%s",
					portType.getNamespaceURI(),
					portType.getLocalPart(),
					name,
					DPWSConstants.INPUT_ACTION_SUFFIX);
		return inputAction;
	}
	
	public String getOutputAction(){
		if(outputAction != null){
			return outputAction;
		}
		outputAction = String.format("%s/%s/%s%s",
				portType.getNamespaceURI(),
				portType.getLocalPart(),
				name,
				isEvent() ? "" : DPWSConstants.OUTPUT_ACTION_SUFFIX);
		return outputAction;

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
	
	public abstract List<SchemaType> getTypes();
	
	public List<TMessage> getMessages(){
		
		List<TMessage> messages = new ArrayList<TMessage>();
		
		if(hasInput){
			messages.add(new TMessage(getName() + "InputMessage",inputElement));
		}
		if(hasOutput){
			messages.add(new TMessage(getName() + "OutputMessage",outputElement));
		}
		if(hasFault){
			messages.add(new TMessage(getName() + "FaultMessage",faultElement));
		}
		return messages;
	}
	
	public TOperation getWsdlOperation(){
//		<operation name="OperationOne">
//		<input message="tns:InputElementOneMessage" />
//		<output message="tns:OutputElementOneMessage" />
//	</operation>
		
		TOperation op =  new TOperation(getName());
		if(hasInput){
			op.addInput(inputElement);
		}

		if(hasOutput){
			op.addOutput(outputElement);
		}
		
		if(hasFault){
			op.addFault(faultElement);
		}
		
		return op;
	}	
	
	public TBindingOperation getWsdlBindingOperation(){
//		
//		<operation name="OperationOne">
//		<soap:operation style="document" />
//		<wsdl:input>
//			<soap:body use="literal" />
//		</wsdl:input>
//		<wsdl:output>
//			<soap:body use="literal" />
//		</wsdl:output>
//	</operation>
		
		TBindingOperation bo = new TBindingOperation(getName());
		bo.addAny(new org.xmlsoap.schemas.wsdl.soap12.TOperation(TStyleChoice.DOCUMENT));

		if(hasInput){
			TBindingOperationMessage bom = new TBindingOperationMessage();
			bom.addAny(new TBody(UseChoice.LITERAL));
			bo.setInput(bom);
		}
		if(hasOutput){
			TBindingOperationMessage bom = new TBindingOperationMessage();
			bom.addAny(new TBody(UseChoice.LITERAL));
			bo.setOutput(bom);
		}
		
		if(hasFault){
			TBindingOperationFault fault = new TBindingOperationFault();
			fault.addAny(new TBody(UseChoice.LITERAL));
			bo.addFault(fault);
		}
		return null;
	}
	
	
	protected void logInfo(String msg){
		logger.info(String.format("[Operation %s] - %s", getName(), msg));
	}
	
	protected void logError(String msg, Throwable e){
		logger.log(Level.SEVERE,String.format("[Operation %s] - %s", getName(), msg),e);
	}
	
	
}
