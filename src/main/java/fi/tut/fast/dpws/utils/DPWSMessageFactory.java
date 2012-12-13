/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.tut.fast.dpws.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.soap.MessageFactoryHelper;

/**
 *
 * @author Johannes
 */
public class DPWSMessageFactory {
    
//    private static DPWSMessageFactory instance;
    private static MessageFactory factory;
    
    public static void init(){
    	if(factory != null){
    		return;
    	}
        try {
        	       
//            factory = MessageFactory.newInstance(DPWSConstants.SOAP_1_2_PROTOCOL);

            factory = MessageFactoryHelper.getInstance().getMessageFactory(DPWSConstants.SOAP_1_2_PROTOCOL);
            
        } catch (SOAPException ex) {
            Logger.getLogger(DPWSMessageFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
    public static SOAPMessage getUnsubscribeMessage(String eventAction, String serviceAddress ) throws SOAPException{
        return getUnsubscribeMessage( eventAction,  serviceAddress,  null);
    }
    
    public static SOAPMessage getUnsubscribeMessage(String eventAction, String serviceAddress, String refId) throws SOAPException{
        
        SOAPMessage msg = createMessageInternal(DPWSConstants.WSE_UNSUBSCRIBE_ACTION,serviceAddress);
        
        if(refId != null){
            msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSE_IDENTIFIER_QNAME)
                    .addAttribute(DPWSConstants.WSA_IS_REFERENCE_PARAMETER_ATTR, "true")
                    .setValue(refId);
        }
              
        msg.getSOAPBody().addBodyElement(DPWSConstants.WSE_UNSUBSCRIBE_QNAME);
        
        return msg;
    }
    
    public static SOAPMessage getSubscribeMessage(String eventAction, String serviceAddress, String deliveryAddress) throws SOAPException{
        return getSubscribeMessage( eventAction,  serviceAddress,  deliveryAddress,  null);
    }
    
    public static SOAPMessage getSubscribeMessage(String eventAction, String serviceAddress, String deliveryAddress, String refId) throws SOAPException{

        SOAPMessage msg = createMessageInternal(DPWSConstants.WSE_SUBSCRIBE_ACTION,serviceAddress);
        
        SOAPBodyElement subscribe = msg.getSOAPBody().addBodyElement(DPWSConstants.WSE_SUBSCRIBE_QNAME);
        SOAPElement notifyTo = subscribe.addChildElement(DPWSConstants.WSE_DELIVERY_QNAME)
                .addAttribute(DPWSConstants.WSE_MODE_ATTR,DPWSConstants.WSE_DELIVERYMODE_PUSH)
                .addChildElement(DPWSConstants.WSE_NOTIFYTO_QNAME);
        
        notifyTo.addChildElement(DPWSConstants.WSA_ADDRESS_QNAME).setValue(deliveryAddress);
        
        if(refId != null){
            notifyTo.addChildElement(DPWSConstants.WSA_REFERENCE_PARAMETERS_QNAME)
                    .addChildElement(DPWSConstants.WSE_IDENTIFIER_QNAME)
                    .setValue(refId);
        }

        subscribe.addChildElement(DPWSConstants.WSE_FILTER_QNAME)
                .addAttribute(DPWSConstants.WSE_DIALECT_ATTR, DPWSConstants.WSE_FILTERDIALECT_ACTION)
                .setValue(eventAction);
        return msg;
    }
    
    public static SOAPMessage getInputMessage(String inputAction, String serviceAddress) throws SOAPException{
        return createMessageInternal(inputAction,serviceAddress);
    }
    
    public static String newUUID(){
        return DPWSConstants.UUID_PREFIX + UUID.randomUUID().toString();
    }
    
    public static SOAPMessage recieveMessage(InputStream in) throws IOException, SOAPException{
        return factory.createMessage(null, in);
    }
    
    public static SOAPMessage recieveMessage(DatagramPacket in) throws IOException, SOAPException{
        return factory.createMessage(null, new ByteArrayInputStream(in.getData()));
    }
    
    public static SOAPMessage getDiscoveryProbe() throws SOAPException{
    	return createMessageInternal(DPWSConstants.WSD_PROBE_ACTION,DPWSConstants.WSD_DISCOVERY_PROBE);
    }
    

    public static SOAPMessage getDiscoveryProbe(String replyTo) throws SOAPException{
    	SOAPMessage msg = getDiscoveryProbe();
//    	msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_REPLY_TO_QNAME)
//    			.addChildElement(DPWSConstants.WSA_ENDPOINT_REFERENCE_QNAME)
//    			.addChildElement(DPWSConstants.WSA_ADDRESS_QNAME)
//    			.setValue(replyTo);
    	msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_REPLY_TO_QNAME)
		.addChildElement(DPWSConstants.WSA_ADDRESS_QNAME)
		.setValue(replyTo);
    	return msg;
    }
    
    // WS-Transfer
    public static SOAPMessage createGetRequest(String to) throws SOAPException{
        return createRequestMessageInternal(DPWSConstants.WST_GET_ACTION,to,DPWSConstants.WSA_ADDRESSING_ROLE_ANONYMOUS);
    }

    public static SOAPMessage createGetResponse(String RelatesTo, String from) throws SOAPException{
        return createResponseMessageInternal(DPWSConstants.WST_GETRESPONSE_ACTION,DPWSConstants.WSA_ADDRESSING_ROLE_ANONYMOUS, RelatesTo, from);
    }

    // Internal Methods
    protected static SOAPMessage createMessageInternal(String action, String to) throws SOAPException{
        SOAPMessage msg = factory.createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        
        // Set SOAP headers, including action ID, recipient, and random identifier.
        msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_ACTION_QNAME).setValue(action);
        msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_TO_QNAME).setValue(to);
        msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_MESSAGE_ID_QNAME).setValue(newUUID());
        return msg;
    }
    
    protected static SOAPMessage createRequestMessageInternal(String action, String to, String replyTo) throws SOAPException{
        SOAPMessage msg = createMessageInternal(action,to);
        msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_REPLY_TO_QNAME).addChildElement(DPWSConstants.WSA_ADDRESS_QNAME).setValue(replyTo);
        return msg;
    }
    
    protected static SOAPMessage createResponseMessageInternal(String action, String to, String RelatesTo, String from) throws SOAPException{
        SOAPMessage msg = createMessageInternal(action,to);
        msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_RELATES_TO_QNAME).setValue(RelatesTo);
        msg.getSOAPHeader().addHeaderElement(DPWSConstants.WSA_FROM_QNAME).addChildElement(DPWSConstants.WSA_ADDRESS_QNAME).setValue(from);
        return msg;
    }
    
}
