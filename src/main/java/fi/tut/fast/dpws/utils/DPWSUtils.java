package fi.tut.fast.dpws.utils;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import org.xmlsoap.schemas.addressing.AttributedURI;
import org.xmlsoap.schemas.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.devprof.LocalizedStringType;

import fi.tut.fast.dpws.DPWSConstants;

public class DPWSUtils {


    public static QName qualify(String namespace, String local){
    	return new QName(namespace,local);
    }
    
    public static LocalizedStringType localize(String value){
    	return new LocalizedStringType(DPWSConstants.LOCALE_EN, value);
    }
    
    public static EndpointReferenceType toEPR(String uri) throws URISyntaxException{
    	EndpointReferenceType epr = new EndpointReferenceType();
    	AttributedURI au = new AttributedURI();
    	au.setValue(new URI(uri));
    	epr.setAddress(au);
    	return epr;
    }
	
    

    
    
    
}
