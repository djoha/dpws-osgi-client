/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.tut.fast.dpws;

import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;

/**
 *
 * @author Johannes
 */
public class DPWSConstants {

    
    public static final String UUID_PREFIX = "urn:uuid:";
    public static final String SOAP_1_2_PROTOCOL = SOAPConstants.SOAP_1_2_PROTOCOL;
    public static final String LOCALE_EN = "en";
    public static final String DEFAULT_LOCALE = Locale.getDefault().getLanguage();
    public static final String EVENT_FILTER_DELIMITER = ",";
    
	public static final int WS_DISOVERY_PORT = 3702;
	public static final String WS_DISCOVERY_MULTICAST_GROUP = "239.255.255.250";
	public static final int WS_DISCOVERY_SOCKET_TIMEOUT = 500; // MS
	public static final int WSD_PROBE_MAX_SIZE = 1024;
    
	public static final String WSD_NAMESPACE = "http://schemas.xmlsoap.org/ws/2005/04/discovery";
	public static final String WSD_PREFIX = "wsd";
    public static final String WSD_DISCOVERY_PROBE = "urn:schemas-xmlsoap-org:ws:2005:04:discovery";
    public static final String WSD_PROBE_ACTION = "http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe";
    public static final String WSD_PROBE_MATCH_ACTION = "http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe";
    public static final QName WSD_PROBE_ELEMENT_QNAME = new QName(WSD_NAMESPACE,"Probe",WSD_PREFIX);
    
    // WS-Addressing
    
    public static final String WSA_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    public static final String WSA_PREFIX = "wsa";
    public static final QName WSA_ADDRESS_QNAME = new QName(WSA_NAMESPACE,"Address",WSA_PREFIX);
    public static final QName WSA_ACTION_QNAME = new QName(WSA_NAMESPACE,"Action",WSA_PREFIX);
    public static final QName WSA_TO_QNAME = new QName(WSA_NAMESPACE,"To",WSA_PREFIX);
    public static final QName WSA_REPLY_TO_QNAME = new QName(WSA_NAMESPACE,"ReplyTo",WSA_PREFIX);
    public static final QName WSA_RELATES_TO_QNAME = new QName(WSA_NAMESPACE,"RelatesTo",WSA_PREFIX);
    public static final QName WSA_FROM_QNAME = new QName(WSA_NAMESPACE,"From",WSA_PREFIX);
    public static final QName WSA_MESSAGE_ID_QNAME = new QName(WSA_NAMESPACE,"MessageID",WSA_PREFIX);
    public static final QName WSA_REFERENCE_PARAMETERS_QNAME = new QName(WSA_NAMESPACE,"ReferenceParameters",WSA_PREFIX);
    public static final QName WSA_IS_REFERENCE_PARAMETER_ATTR = new QName(WSA_NAMESPACE,"IsReferenceParameter",WSA_PREFIX);
    public static final QName WSA_ENDPOINT_REFERENCE_QNAME = new QName(WSA_NAMESPACE,"EndpointReference",WSA_PREFIX);

    
    
    public static final String WSA_ADDRESSING_ROLE_ANONYMOUS = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";
    
    // WS-Eventing
    public static final String WSE_SUBSCRIBE_ACTION = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe";
    public static final String WSE_UNSUBSCRIBE_ACTION = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe";
    public static final String WSE_DELIVERYMODE_PUSH = "http://schemas.xmlsoap.org/ws/2004/08/eventing/DeliveryModes/Push";
    public static final String WSE_FILTERDIALECT_ACTION = "http://schemas.xmlsoap.org/ws/2006/02/devprof/Action";
    public static final String WSE_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/eventing";
    public static final String WSE_PREFIX = "wse";

    public static final QName WSE_DIALECT_ATTR = new QName(WSE_NAMESPACE,"Dialect",WSE_PREFIX);
    public static final QName WSE_MODE_ATTR = new QName(WSE_NAMESPACE,"Mode",WSE_PREFIX);
    public static final QName WSE_SUBSCRIBE_QNAME = new QName(WSE_NAMESPACE,"Subscribe",WSE_PREFIX);
    public static final QName WSE_UNSUBSCRIBE_QNAME = new QName(WSE_NAMESPACE,"Unsubscribe",WSE_PREFIX);
    public static final QName WSE_DELIVERY_QNAME = new QName(WSE_NAMESPACE,"Delivery",WSE_PREFIX);
    public static final QName WSE_NOTIFYTO_QNAME = new QName(WSE_NAMESPACE,"NotifyTo",WSE_PREFIX);
    public static final QName WSE_FILTER_QNAME = new QName(WSE_NAMESPACE,"Filter",WSE_PREFIX);
    public static final QName WSE_IDENTIFIER_QNAME = new QName(WSE_NAMESPACE,"Identifier",WSE_PREFIX);
    public static final QName WSE_SUBSCRIPTION_MANAGER_QNAME = new QName(WSE_NAMESPACE,"SubscriptionManager",WSE_PREFIX);
    public static final QName WSE_EXPIRES_QNAME = new QName(WSE_NAMESPACE,"Expires",WSE_PREFIX);
    
    //WS-Transfer
    public static final String WST_GETRESPONSE_ACTION = "http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse";
    public static final String WST_GET_ACTION = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Get";
    
    //WS_MetadataExchange
    public static final String METADATA_SECTION_DIALECT_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_PREFIX = "wsd";
	public static final QName WSDL_TYPES_QNAME = new QName(WSDL_NAMESPACE,"types",WSDL_PREFIX);
    

	public static final String OUTPUT_ACTION_SUFFIX = "Response";
	public static final String INPUT_ACTION_SUFFIX = "Request";
	
    public static final QName EMPTY_MESSAGE_QNAME = new QName("EMPTY_MESSAGE","EMPTY_MESSAGE");
	
    
    // From DPWS Appendix D
    public static final int APP_MAX_DELAY = 5000 ;
    public static final int DISCOVERY_PORT = 3702;
    public static final int MATCH_TIMEOUT = 10 ;
    public static final int MAX_ENVELOPE_SIZE = 32767 ;
    public static final int MAX_FIELD_SIZE = 256 ;
    public static final int MAX_URI_SIZE = 2048 ;
    public static final int MULTICAST_UDP_REPEAT = 2;
    public static final int UDP_MAX_DELAY = 250 ;
    public static final int UDP_MIN_DELAY = 50 ;
    public static final int UDP_UPPER_DELAY = 450 ;
    public static final int UNICAST_UDP_REPEAT = 2;

    
    
    private DPWSConstants(){}
    
}
