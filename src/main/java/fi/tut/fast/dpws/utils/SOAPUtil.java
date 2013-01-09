package fi.tut.fast.dpws.utils;

import java.util.Iterator;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Node;

import fi.tut.fast.dpws.DPWSConstants;

public class SOAPUtil {

	public static String getActionHeader(SOAPMessage msg) throws SOAPException{
		for(Iterator<Node> nodes = msg.getSOAPHeader().getChildElements(DPWSConstants.WSA_ACTION_QNAME);
				nodes.hasNext(); ){
			return nodes.next().getTextContent();
		}
		return null;
	}
	
	public static String getWseIdentifierHeader(SOAPMessage msg) throws SOAPException{
		for(Iterator<Node> nodes = msg.getSOAPHeader().getChildElements(DPWSConstants.WSE_IDENTIFIER_QNAME);
				nodes.hasNext(); ){
			return nodes.next().getTextContent();
		}
		return null;
	}
	
}
