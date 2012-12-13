package fi.tut.fast.dpws.device;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.xmlsoap.schemas.wsdl.TMessage;
import org.xmlsoap.schemas.wsdl.TOperation;

public abstract class Operation<I,O,F> {

	String name;
	String namespace;
	Service parent;
	protected Class<I> inputType;
	protected Class<O> outputType;
	protected Class<F> faultType;
	
	
	
	public abstract O invoke(I input);
	
	public Operation(String name, String namespace, Class<I> iClazz, Class<O> oClazz, Class<F> fClazz){
		this.name = name;
		this.namespace = namespace;
		this.inputType = iClazz;
		this.outputType = oClazz;
		this.faultType = fClazz;

	}
	
	public List<TMessage> getMessages(){
		List<TMessage> messages  = new ArrayList<TMessage>();
		if(hasInput()){
			messages.add(new TMessage(inputMessageName(name,namespace).getLocalPart(),new QName(namespace,inputType.getSimpleName())));
		}
		if(hasOutput()){
			messages.add(new TMessage(outputMessageName(name,namespace).getLocalPart(),new QName(namespace,outputType.getSimpleName())));
		}
		if(hasFault()){
			messages.add(new TMessage(faultMessageName(name,namespace).getLocalPart(),new QName(namespace,faultType.getSimpleName())));
		}
		return messages;
	}
//	
//	protected void loadTypes(TTypes types){
//		
//		
//		
//	}
	
	
	public TOperation getTOperation(){
		TOperation op = new TOperation(name);
		if(inputType != null){
			op.addInput(inputMessageName(name,namespace));
		}
		if(outputType != null){
			op.addOutput(outputMessageName(name,namespace));
		}
		if(faultType != null){
			op.addFault(faultMessageName(name,namespace));
		}
		return op;
	}

	public boolean hasInput(){
		return (inputType != null);
	}
	public boolean hasOutput(){
		return (outputType != null);
	}
	public boolean hasFault(){
		return (faultType != null);
	}
	

	protected static QName inputMessageName(String opName, String namespace){
		return new QName(namespace, opName + "InMsg");
	}
	protected static QName outputMessageName(String opName, String namespace){
		return new QName(namespace, opName + "OutMsg");
	}
	protected static QName faultMessageName(String opName, String namespace){
		return new QName(namespace, opName + "FaultMsg");
	}
	
	protected void setTOperation(TOperation op){
		
		
	}
	
	
}
