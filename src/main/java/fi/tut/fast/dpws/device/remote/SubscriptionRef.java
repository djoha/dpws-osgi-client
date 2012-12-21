package fi.tut.fast.dpws.device.remote;

import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

public class SubscriptionRef {

	String id;
	OperationReference operation;
	Status status;
	String endpoint;
	
	protected SubscriptionRef(String id, OperationReference operation){
		this.id = id;
		this.operation = operation;
	}
	
	public SubscriptionRef(String id, String endpoint, OperationReference operation){
		this.id = id;
		this.operation = operation;
		this.endpoint = endpoint;
	}
	
	public static SubscriptionRef subscribe(String deliverTo, OperationReference operation) throws SOAPException, XMLStreamException{
	
		String id = operation.subscribe(deliverTo);
		
		SubscriptionRef ref = new SubscriptionRef(id, operation);
		ref.setDeliverTo(deliverTo);
		
		return ref;
	}
	
	public void unsubscribe() throws SOAPException{
		operation.unsubscribe(id);
	}
	
	public String getId(){
		return id;
	}
	
	public OperationReference getOperation(){
		return operation;
	}
	
	public Status getStatus(){
		return status;
	}

	public String getDeliverTo(){
		return endpoint;
	}
	
	protected void setDeliverTo(String endpoint){
		this.endpoint = endpoint;
	}
	
	public enum Status{
		ACTIVE,EXPIRED,ENDED
	}
	
}
