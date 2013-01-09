package fi.tut.fast.dpws.device.remote;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.xmlsoap.schemas.eventing.SubscribeResponse;

import fi.tut.fast.dpws.utils.DPWSCommunication;
import fi.tut.fast.dpws.utils.DPWSMessageFactory;
import fi.tut.fast.dpws.utils.DPWSXmlUtil;

public class SubscriptionRef {

	private String id;
	private String notifyTo;
	private String serviceAddress;
	private String filter;
	private Status status;
	
	
	private SubscriptionRef(String filter, String id, String notifyTo, String serviceAddress){		

		this.id = id;
		this.notifyTo = notifyTo;
		this.serviceAddress = serviceAddress;
		this.filter = filter;
		
		status = Status.SUBSCRIBED;
	}
	
	public static SubscriptionRef subscribe(String filter, String notifyTo, String serviceAddress) throws SOAPException, JAXBException, IOException{
		
		String refId = DPWSMessageFactory.newUUID();
		
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage msg = DPWSMessageFactory.getSubscribeMessage(filter,
												serviceAddress,
												notifyTo,
												refId);
		SOAPMessage resp = conn.call(msg, serviceAddress);
	
		SubscribeResponse subResp = (SubscribeResponse) DPWSXmlUtil
				.getInstance().unmarshalSoapBody(resp);
		String refIdOut = subResp.getSubscriptionManager().getReferenceParameters().getIdentifier();
		
		return new SubscriptionRef(filter, refIdOut, notifyTo, serviceAddress);
		
	}

	public String getId(){
		return id;
	}
	
	public void unsubscribe() throws SOAPException{
		SOAPConnection conn = DPWSCommunication.getNewSoapConnection();
		SOAPMessage unsub = DPWSMessageFactory.getUnsubscribeMessage(filter, serviceAddress, id);
		SOAPMessage resp = conn.call(unsub, serviceAddress);
		status = Status.ENDED;
	}
	
	public Status getStatus(){
		return status;
	}
	
	public enum Status {
		SUBSCRIBED, EXPIRED, ENDED, UNKNOWN
	}
	
	public String toString(){
		return String.format("[%s] %s {%s} -> %s", id,serviceAddress, filter, notifyTo);
	}
	
	public long sendGetStatus(){
		return 0;
	}
	
	public boolean renew(){
		return false;
	}
	
}
