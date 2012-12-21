//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.12.20 at 02:07:56 PM CET 
//


package org.xmlsoap.schemas.wsdl.soap12;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.xmlsoap.schemas.wsdl.soap12 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Operation_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "operation");
    private final static QName _Body_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "body");
    private final static QName _Address_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "address");
    private final static QName _Binding_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "binding");
    private final static QName _Header_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "header");
    private final static QName _Headerfault_QNAME = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "headerfault");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.xmlsoap.schemas.wsdl.soap12
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TBody }
     * 
     */
    public TBody createTBody() {
        return new TBody();
    }

    /**
     * Create an instance of {@link TOperation }
     * 
     */
    public TOperation createTOperation() {
        return new TOperation();
    }

    /**
     * Create an instance of {@link TAddress }
     * 
     */
    public TAddress createTAddress() {
        return new TAddress();
    }

    /**
     * Create an instance of {@link TBinding }
     * 
     */
    public TBinding createTBinding() {
        return new TBinding();
    }

    /**
     * Create an instance of {@link THeaderFault }
     * 
     */
    public THeaderFault createTHeaderFault() {
        return new THeaderFault();
    }

    /**
     * Create an instance of {@link THeader }
     * 
     */
    public THeader createTHeader() {
        return new THeader();
    }

    /**
     * Create an instance of {@link TExtensibilityElementOpenAttrs }
     * 
     */
    public TExtensibilityElementOpenAttrs createTExtensibilityElementOpenAttrs() {
        return new TExtensibilityElementOpenAttrs();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TOperation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/soap12/", name = "operation")
    public JAXBElement<TOperation> createOperation(TOperation value) {
        return new JAXBElement<TOperation>(_Operation_QNAME, TOperation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TBody }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/soap12/", name = "body")
    public JAXBElement<TBody> createBody(TBody value) {
        return new JAXBElement<TBody>(_Body_QNAME, TBody.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TAddress }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/soap12/", name = "address")
    public JAXBElement<TAddress> createAddress(TAddress value) {
        return new JAXBElement<TAddress>(_Address_QNAME, TAddress.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TBinding }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/soap12/", name = "binding")
    public JAXBElement<TBinding> createBinding(TBinding value) {
        return new JAXBElement<TBinding>(_Binding_QNAME, TBinding.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link THeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/soap12/", name = "header")
    public JAXBElement<THeader> createHeader(THeader value) {
        return new JAXBElement<THeader>(_Header_QNAME, THeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link THeaderFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/wsdl/soap12/", name = "headerfault")
    public JAXBElement<THeaderFault> createHeaderfault(THeaderFault value) {
        return new JAXBElement<THeaderFault>(_Headerfault_QNAME, THeaderFault.class, null, value);
    }

}
