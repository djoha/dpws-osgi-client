package fi.tut.fast.dpws.soap;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;

import com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl;

public class MessageFactoryHelper extends SAAJMetaFactoryImpl {

	private static MessageFactoryHelper instance;
	private MessageFactoryHelper(){
	}
	
	public static MessageFactoryHelper getInstance(){
		if(instance ==  null){
			instance =  new MessageFactoryHelper();
		}
		return instance;
	}
	
	public MessageFactory getMessageFactory(String soapVersion) throws SOAPException{		
		return newMessageFactory(soapVersion);
	}
	
	
}
