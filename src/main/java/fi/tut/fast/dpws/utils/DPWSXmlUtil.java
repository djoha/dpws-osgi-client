package fi.tut.fast.dpws.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.addressing.AttributedURI;
import org.xmlsoap.schemas.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.devprof.HostServiceType;
import org.xmlsoap.schemas.discovery.ByeType;
import org.xmlsoap.schemas.discovery.HelloType;
import org.xmlsoap.schemas.wsdl.TDefinitions;
import org.xmlsoap.schemas.wsdl.TTypes;

import fi.tut.fast.dpws.DPWSConstants;
//import com.sun.tools.xjc.Language;
//import com.sun.tools.xjc.Options;
//import com.sun.tools.xjc.Plugin;
//import com.sun.tools.xjc.api.ErrorListener;
//import com.sun.tools.xjc.api.Mapping;
//import com.sun.tools.xjc.api.S2JJAXBModel;
//import com.sun.tools.xjc.api.SchemaCompiler;
//import com.sun.tools.xjc.api.XJC;
//import com.sun.tools.xjc.outline.Outline;

public class DPWSXmlUtil {

	private static final transient Logger logger = Logger
			.getLogger(DPWSXmlUtil.class.getName());

	private static DPWSXmlUtil instance;
	private JAXBContext context;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private org.xmlsoap.schemas.devprof.ObjectFactory defProfFactory;
	private org.xmlsoap.schemas.mex.ObjectFactory mexObjFactory;
	private org.xmlsoap.schemas.addressing.ObjectFactory wsaObjFactory;
	private org.xmlsoap.schemas.discovery.ObjectFactory wsdObjFactory;
	private BundleContext bundleContext;
	private static boolean isInitialized = false;
	private List<XmlError> errs;

	private static String ROOT_ELEMENT = "/";

	public static void init(BundleContext bundleContext) throws JAXBException {
		if (instance == null) {
			instance = new DPWSXmlUtil(bundleContext);
		}
	}

	public static DPWSXmlUtil getInstance() throws JAXBException {
		if (instance == null) {
			instance = new DPWSXmlUtil();
			// throw new
			// IllegalStateException("DPWSXmlUtil is not initialized.");
		}
		if (!isInitialized) {
			instance.initInternal();
		}
		return instance;
	}

	private DPWSXmlUtil(BundleContext bundleContext) throws JAXBException {
		this.bundleContext = bundleContext;
	}

	private void initInternal() throws JAXBException {
		defProfFactory = new org.xmlsoap.schemas.devprof.ObjectFactory();
		mexObjFactory = new org.xmlsoap.schemas.mex.ObjectFactory();
		wsaObjFactory = new org.xmlsoap.schemas.addressing.ObjectFactory();
		wsdObjFactory = new org.xmlsoap.schemas.discovery.ObjectFactory();

		StringBuilder contextPath = new StringBuilder();
		contextPath.append("org.xmlsoap.schemas.addressing:");
		contextPath.append("org.xmlsoap.schemas.devprof:");
		contextPath.append("org.xmlsoap.schemas.discovery:");
		contextPath.append("org.xmlsoap.schemas.eventing:");
		contextPath.append("org.xmlsoap.schemas.mex:");
		contextPath.append("org.xmlsoap.schemas.transfer:");
		contextPath.append("org.xmlsoap.schemas.wsdl:");
		contextPath.append("org.w3c.schemas.wsaddr");

		context = JAXBContext.newInstance(contextPath.toString());
		marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		unmarshaller = context.createUnmarshaller();
		isInitialized = true;
	}

	private DPWSXmlUtil() throws JAXBException {

	}

	public Object unmarshalSoapBody(SOAPMessage msg) throws JAXBException,
			SOAPException, IOException {
		Object obj = unmarshaller.unmarshal(msg.getSOAPBody()
				.extractContentAsDocument());
		if (obj instanceof JAXBElement) {
			return ((JAXBElement) obj).getValue();
		}
		return obj;
	}

	public Object unmarshalSoapBody(InputStream is) throws JAXBException,
			SOAPException, IOException {
		return unmarshalSoapBody(DPWSMessageFactory.recieveMessage(is));
	}

	public Object unmarshalSoapBody(Exchange ex) throws JAXBException,
			SOAPException, IOException {
		return unmarshalSoapBody(ex.getIn().getBody(InputStream.class));
	}

	public TDefinitions unmarshalWSDL(Reader in) throws JAXBException {
		return (TDefinitions) unwrap(unmarshaller.unmarshal(in));
	}

	public EndpointReferenceType createEndpointReference(URI address) {
		AttributedURI auri = wsaObjFactory.createAttributedURI();
		auri.setValue(address);
		EndpointReferenceType eprt = wsaObjFactory
				.createEndpointReferenceType();
		eprt.setAddress(auri);
		return eprt;
	}

	public JAXBElement<HelloType> createHello(HelloType hello) {
		return wsdObjFactory.createHello(hello);
	}

	public JAXBElement<ByeType> createBye(ByeType hello) {
		return wsdObjFactory.createBye(hello);
	}

	public void marshall(Object o, OutputStream out) throws JAXBException {
		marshaller.marshal(o, out);
	}

	public void marshall(Object o, Writer out) throws JAXBException {
		marshaller.marshal(o, out);
	}

	public JAXBElement<HostServiceType> createHosted(HostServiceType hosted) {
		return defProfFactory.createHosted(hosted);
	}

    private static XPathFactory xfactory;
    public static String extractReferenceParam(SOAPMessage subscriptionResponse) throws XPathExpressionException, XMLStreamException, SOAPException{

        if(xfactory == null){
        	xfactory = XPathFactory.newInstance();
        }
        XPath path = xfactory.newXPath();
        NamespaceContext ctx = new UniversalNamespaceResolver(subscriptionResponse.getSOAPPart());
        path.setNamespaceContext(ctx);
        String expr = "/s12:Envelope/s12:Body/wse:SubscribeResponse/wse:SubscriptionManager/wsa:ReferenceParameters/wse:Identifier";
        XPathExpression expression = path.compile(expr);
        return (String) expression.evaluate((Node)subscriptionResponse.getSOAPPart(),XPathConstants.STRING);

    }
	
	public Object unwrap(Object o) {
		if (o instanceof JAXBElement) {
			return ((JAXBElement) o).getValue();
		}
		return o;
	}

	public TypeHandler compileTypeSchemas(final String systemId,
			List<TTypes> types) throws XmlException, IOException {

		List<XmlObject> schemas = new ArrayList<XmlObject>();

		errs = new ArrayList<XmlError>();

		XmlOptions opts = new XmlOptions();
		opts.setErrorListener(errs);

		for (TTypes type : types) {
			for (Object o : type.getAny()) {
				if (o instanceof Element) {

					XmlObject obj = SchemaDocument.Factory.parse((Element) o,
							opts);
					schemas.add(obj);

				}
			}
		}

		opts = new XmlOptions();
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(4);
		opts.setUseDefaultNamespace();
		opts.setSaveOuter();
		opts.setErrorListener(errs);

		// SchemaTypeSystem sts = XmlBeans.compileXsd(schemas.toArray(new
		// XmlObject[]{}), XmlBeans.getContextTypeLoader(), opts);

		SchemaTypeLoader stl = XmlBeans.loadXsd(
				schemas.toArray(new XmlObject[] {}), opts);

		// System.out.format("\nSchema Compiled: %d errors %d Elements, %d Types\n",
		// errs.size(), sts.globalElements().length , sts.globalTypes().length
		// );

		for (XmlError err : errs) {
			System.err.println(err.getMessage());
		}
		//
		// for(SchemaGlobalElement el : st.globalElements()){
		// System.out.format("Element: %s (type: %s)\n", el.getName(),
		// el.getType().getName());
		// }
		//
		// SchemaType t = sts.globalElements()[1].getType();
		// schem
		//

		// Map<String,String> params = new HashMap<String,String>();
		//
		// params.put("K1", "4.2");
		// params.put("K2", "6.3");
		// params.put("@lang", "FR");
		//
		// XmlObject obj = buildAndPopulateElement(new
		// QName("http://www.tut.fi/wsdl/SomeService",
		// "InputElementOne"), stl,params);
		//
		// writeXml(obj.getDomNode());
		//
		// obj = buildAndPopulateElement(new
		// QName("http://www.tut.fi/wsdl/SomeService",
		// "OutputElementOne"), stl,"SomeOutput");
		//
		// obj = buildElement(new QName("http://www.tut.fi/wsdl/SomeService",
		// "OperationTwo"), stl);
		// obj = buildElement(new QName("http://www.tut.fi/wsdl/SomeService",
		// "OperationTwoResponse"), stl);
		// obj = buildElement(new QName("http://www.tut.fi/wsdl/SomeService",
		// "somethingHappened"), stl);

		return new TypeHandler(systemId, stl);
	}

	public XmlObject buildElement(QName name, SchemaTypeLoader stl) {
		SchemaGlobalElement el = stl.findElement(name);
		SchemaType elType = el.getType();
//
//		XmlObject obj = stl.newInstance(elType, new XmlOptions()
//						.setDocumentType(elType));
	
		XmlObject obj = XmlObject.Factory.newInstance();
		XmlCursor c = obj.newCursor();

		c.toNextToken();
		buildElement(c, name, elType, stl);
		c.toEndDoc();

		return obj;
	}

	private void buildElement(XmlCursor c, QName name, SchemaType elType,
			SchemaTypeLoader stl) {

		c.beginElement(name);
		if (elType.getAttributeModel() != null) {
			for (SchemaLocalAttribute attr : elType.getAttributeModel()
					.getAttributes()) {
				c.insertAttributeWithValue(attr.getName(),
						getDefaultText(attr.getType()));
			}
		}

		switch (elType.getContentType()) {
		case SchemaType.NOT_COMPLEX_TYPE:
			c.insertChars(getDefaultText(elType));
			break;
		case SchemaType.SIMPLE_CONTENT:
			c.insertChars(getDefaultText(elType.getBaseType()));
			break;
		case SchemaType.EMPTY_CONTENT:
			break;
		case SchemaType.ELEMENT_CONTENT:
		case SchemaType.MIXED_CONTENT:
			buildXmlObject(c, elType.getContentModel(), stl);
			break;
		default:
			logger.warning(name + " not recognized Content Type");
			return;
		}
		c.toNextToken();

	}

	private void buildXmlObject(XmlCursor c, SchemaParticle particle,
			SchemaTypeLoader stl) {

		switch (particle.getParticleType()) {
		case SchemaParticle.CHOICE:
			buildXmlObject(c, particle.getParticleChildren()[0], stl);
			break;
		case SchemaParticle.ALL:
		case SchemaParticle.SEQUENCE:
			for (SchemaParticle subParticle : particle.getParticleChildren()) {
				buildXmlObject(c, subParticle, stl);
			}
			break;
		case SchemaParticle.ELEMENT:
			for (int ii = 0; ii < particle.getMinOccurs().intValue(); ii++) {
				buildElement(c, particle.getName(), particle.getType(), stl);
			}
			break;
		case SchemaParticle.WILDCARD:
			break;
		}

	}

	public XmlObject buildAndPopulateElement(QName name, SchemaTypeLoader stl,
			String value) {

		XmlObject obj = buildElement(name, stl);
		XmlCursor c = obj.newCursor();
		c.toNextToken();
		c.setTextValue(value);
		return obj;

	}

	public XmlObject buildAndPopulateElement(QName name, SchemaTypeLoader stl,
			Map<String, String> params) {

		XmlObject obj = buildElement(name, stl);

		XmlCursor c = obj.newCursor();
		c.toNextToken();

		XmlBookmark bookmark = new XmlBookmark() {
		};
		c.setBookmark(bookmark);

		for (Entry<String, String> param : params.entrySet()) {
			boolean skip = false;
			for (String next : param.getKey().split("/")) {
				if (next.startsWith("@")) {
					c.setAttributeText(new QName("", next.substring(1)),
							param.getValue());
					skip = true;
					break;
				} else if (!c.toChild(new QName(name.getNamespaceURI(), next))) {
					logger.warning(name.toString() + " : Path not found: "
							+ param.getKey());
					skip = true;
					break;
				}
			}
			if (!skip) {
				c.setTextValue(param.getValue());
			}
			c.toBookmark(bookmark);
		}

		return obj;
	}

	public XmlObject buildAndPopulateElement(QName name, SchemaTypeLoader stl,
			Map<String, String> params, String path) {
		SchemaGlobalElement el = stl.findElement(name);
		SchemaType elType = el.getType();

		XmlObject obj = stl.newInstance(elType, new XmlOptions()
				.setSavePrettyPrint().setSavePrettyPrintIndent(4));
		XmlCursor c = obj.newCursor();

		c.toNextToken();
		c.beginElement(name);

		switch (elType.getContentType()) {
		case SchemaType.NOT_COMPLEX_TYPE:
		case SchemaType.SIMPLE_CONTENT:
			String val = params.get(ROOT_ELEMENT);
			c.insertChars(val);
			break;
		case SchemaType.EMPTY_CONTENT:
			break;
		case SchemaType.ELEMENT_CONTENT:
		case SchemaType.MIXED_CONTENT:
			buildAndPopulateXmlObject(c, elType.getContentModel(), stl, params,
					path);
			break;
		default:
			logger.warning(name + " not recognized Content Type");
		}
		c.toEndDoc();

		writeXml(obj.getDomNode());

		return obj;
	}

	// public XmlObject buildAndPopulateElement(QName name, SchemaTypeLoader
	// stl, Map<String,String> params, String path) {
	// SchemaGlobalElement el = stl.findElement(name);
	// SchemaType elType = el.getType();
	//
	// XmlObject obj = stl.newInstance(elType,
	// new XmlOptions()
	// .setSavePrettyPrint()
	// .setSavePrettyPrintIndent(4));
	// XmlCursor c = obj.newCursor();
	//
	// c.toNextToken();
	// c.beginElement(name);
	//
	// switch (elType.getContentType()) {
	// case SchemaType.NOT_COMPLEX_TYPE:
	// case SchemaType.SIMPLE_CONTENT:
	// String val = params.get(ROOT_ELEMENT);
	// c.insertChars(val);
	// break;
	// case SchemaType.EMPTY_CONTENT:
	// break;
	// case SchemaType.ELEMENT_CONTENT:
	// case SchemaType.MIXED_CONTENT:
	// buildAndPopulateXmlObject(c, elType.getContentModel(), stl, params,
	// path);
	// break;
	// default:
	// logger.warning(name + " not recognized Content Type");
	// }
	// c.toEndDoc();
	//
	// writeXml(obj.getDomNode());
	//
	// return obj;
	// }

	private void buildAndPopulateXmlObject(XmlCursor c,
			SchemaParticle particle, SchemaTypeLoader stl,
			Map<String, String> params, String path) {

		switch (particle.getParticleType()) {
		case SchemaParticle.CHOICE:
			buildAndPopulateXmlObject(c, particle.getParticleChildren()[0],
					stl, params, path);
			break;
		case SchemaParticle.ALL:
		case SchemaParticle.SEQUENCE:
			for (SchemaParticle subParticle : particle.getParticleChildren()) {
				buildAndPopulateXmlObject(c, subParticle, stl, params, path);
			}
			break;
		case SchemaParticle.ELEMENT:
			for (int ii = 0; ii < particle.getMinOccurs().intValue(); ii++) {
				c.insertElementWithText(particle.getName(),
						getDefaultText(particle));
			}
			break;
		case SchemaParticle.WILDCARD:
			break;
		}

	}

	private String getDefaultText(SchemaParticle particle) {
		String defaultvalue = particle.getDefaultText();
		if (defaultvalue == null) {
			defaultvalue = getDefaultText(particle.getType());
		}
		return defaultvalue;
	}

	private String getDefaultText(SchemaType type) {
		String defaultvalue = "";
		if (type.isBuiltinType()) {
			defaultvalue = getBuiltInDefaultText(type);
		} else {
			XmlAnySimpleType[] enumVals = type.getEnumerationValues();
			if (enumVals != null) {
				defaultvalue = enumVals[0].getStringValue();
			}
		}
		return defaultvalue;
	}

	public void analyzeElement(QName name, SchemaTypeLoader stl) {

		SchemaGlobalElement el = stl.findElement(name);

		SchemaType elType = el.getType();

		XmlObject obj = stl.newInstance(elType, new XmlOptions());
		NodeList nl = obj.getDomNode().getChildNodes();
		for (int ii = 0; ii < nl.getLength(); ii++) {
			System.out.println("Child Node: " + nl.item(ii).getNodeName());
		}

		for (SchemaType sub : elType.getAnonymousTypes()) {
			System.out.println("SubType: " + sub.getName());
		}

		// switch(elType.getComponentType()){
		// //Returns the type code for the schema object, either TYPE, ELEMENT,
		// ATTRIBUTE, ATTRIBUTE_GROUP, MODEL_GROUP, IDENTITY_CONSTRAINT, or
		// NOTATION.
		// case SchemaType.TYPE:
		// SchemaType.class.getFields()[0].
		// break;
		// case SchemaType.ELEMENT:
		// System.out.println("Content Simple.");
		//
		// case SchemaType.ELEMENT_CONTENT:
		// System.out.println("Content Element.");
		//
		// case SchemaType.MIXED_CONTENT:
		// System.out.println("Content Mixed.");
		// analyzeComplexElement(elType.getContentModel(),stl);
		// break;
		// default:
		// System.out.println(name + " not recognized componentType");
		// //nope.
		// }

		switch (elType.getContentType()) {
		case SchemaType.NOT_COMPLEX_TYPE:
			System.out.println("Not Complex: " + elType.toString());
			break;
		case SchemaType.EMPTY_CONTENT:
			System.out.println("Content Empty." + elType.toString());
			break;
		case SchemaType.SIMPLE_CONTENT:
			System.out.println("Content Simple." + elType.toString());
			break;

		case SchemaType.ELEMENT_CONTENT:
			System.out.println("Content Element." + elType.toString());
			analyzeComplexElement(elType.getContentModel(), stl);
			break;
		case SchemaType.MIXED_CONTENT:
			System.out.println("Content Mixed." + elType.toString());
			analyzeComplexElement(elType.getContentModel(), stl);
			break;
		default:
			System.out.println(name + " not recognized componentType");
			// nope.
		}

		writeXml(obj.getDomNode());
		System.out.println();

	}

	private void analyzeComplexElement(SchemaParticle particle,
			SchemaTypeLoader stl) {

		switch (particle.getParticleType()) {
		case SchemaParticle.ALL:
		case SchemaParticle.CHOICE:
		case SchemaParticle.SEQUENCE:
			for (SchemaParticle subParticle : particle.getParticleChildren()) {
				analyzeComplexElement(subParticle, stl);
			}
			break;
		case SchemaParticle.ELEMENT:
			System.out.format("Element :%s (Type %s) \n", particle.getName(),
					particle.getType());
			break;
		case SchemaParticle.WILDCARD:
			break;
		}

	}

//	public TypeHandler compileTypeSchemasJAX(final String systemId,
//			List<TTypes> types) throws IOException, JAXBException {
//
//		if (bundleContext == null) {
//			throw new IllegalStateException(
//					"DPWSXmlUtil is not initialized with the Bundle Context.");
//		}
//
//		// Get Binding File
//
//		URL bindingUrl = bundleContext.getBundle().getResource(
//				"/xjc/binding.xjb");
//		InputSource bind = new InputSource(bindingUrl.openStream());
//		bind.setSystemId(systemId);
//
//		// Compile Schemas to Schema Model
//		SchemaCompiler compiler = XJC.createSchemaCompiler();
//		compiler.setErrorListener(new ErrorListener() {
//
//			@Override
//			public void error(SAXParseException ex) {
//				logger.log(Level.SEVERE,
//						"Caught warning while compiling schema.", ex);
//			}
//
//			@Override
//			public void fatalError(SAXParseException ex) {
//				logger.log(Level.SEVERE,
//						"Caught warning while compiling schema.", ex);
//			}
//
//			@Override
//			public void info(SAXParseException ex) {
//				logger.log(Level.INFO, "Info during Schema compile:", ex);
//			}
//
//			@Override
//			public void warning(SAXParseException ex) {
//				logger.log(Level.WARNING,
//						"Caught warning while compiling schema.", ex);
//			}
//
//		});
//
//		// Set compiler options
//		Options xjcOpts = compiler.getOptions();
//		xjcOpts.setSchemaLanguage(Language.XMLSCHEMA);
//		xjcOpts.compatibilityMode = Options.EXTENSION;
//		xjcOpts.addBindFile(bind);
//
//		for (TTypes type : types) {
//			for (Object o : type.getAny()) {
//				if (o instanceof Element) {
//					compiler.parseSchema(systemId, (Element) o);
//				}
//			}
//		}
//
//		// Finalize schema model
//		S2JJAXBModel model = compiler.bind();
//
//		
//		// Generate code model from schema model
//		JCodeModel codeModel = model.generateCode(new Plugin[] {},
//				new ErrorListener() {
//
//					@Override
//					public void error(SAXParseException ex) {
//						logger.log(Level.SEVERE,
//								"Caught warning while generating code.", ex);
//					}
//
//					@Override
//					public void fatalError(SAXParseException ex) {
//						logger.log(Level.SEVERE,
//								"Caught warning while generating code.", ex);
//					}
//
//					@Override
//					public void info(SAXParseException ex) {
//						logger.log(Level.INFO, "Info during code generation:",
//								ex);
//					}
//
//					@Override
//					public void warning(SAXParseException ex) {
//						logger.log(Level.WARNING,
//								"Caught warning while generating code.", ex);
//					}
//
//				});
//
//		logger.info("Bulding Classes...");
//
//		
//		// build classes in some directory in the class folder
//		File dir = bundleContext.getDataFile(systemId);
//		dir.mkdir();
//		codeModel.build(dir);
//
//		// Use java compiler API
//		javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
//
//		DiagnosticListener listener = new DiagnosticListener() {
//			public void report(Diagnostic diagnostic) {
//
//				StringBuilder sb = new StringBuilder();
//				sb.append("Code->" + diagnostic.getCode());
//				sb.append("Column Number->" + diagnostic.getColumnNumber());
//				sb.append("End Position->" + diagnostic.getEndPosition());
//				sb.append("Kind->" + diagnostic.getKind());
//				sb.append("Line Number->" + diagnostic.getLineNumber());
//				sb.append("Message->" + diagnostic.getMessage(Locale.ENGLISH));
//				sb.append("Position->" + diagnostic.getPosition());
//				sb.append("Source" + diagnostic.getSource());
//				sb.append("Start Position->" + diagnostic.getStartPosition());
//				sb.append("\n");
//
//				logger.info(sb.toString());
//			}
//		};
//
//		StandardJavaFileManager fileManager = javac.getStandardFileManager(
//				listener, Locale.ENGLISH, Charset.forName("UTF-8"));
//
//		// Find java files in directory you generated th classes into
//		Collection<File> files = FileUtils.listFiles(dir,
//				new String[] { "java" }, true);
//		Iterable<? extends JavaFileObject> fileObjects = fileManager
//				.getJavaFileObjectsFromFiles(files);
//		
//		// make compilation task
//		CompilationTask task = javac.getTask(null, fileManager, listener, null,
//				null, fileObjects);
//		// run it
//		Boolean result = task.call(); // Line 7
//		if (result == true) {
//			System.out.println("Compilation has succeeded");
//		}
//
//		
//		// make list of class files
//		Collection<File> classes = FileUtils.listFiles(dir,
//				new String[] { "class" }, true);
//
//		List<URL> urlList = new ArrayList<URL>();
//		for (File c : classes) {
//			if (!urlList.contains(c.getParentFile().toURI().toURL())) {
//				urlList.add(c.getParentFile().toURI().toURL());
//			}
//		}
//
//		// Make JAXB context path (list of packages, separated by :)
//		StringBuilder contextPath = new StringBuilder();
//		for (JClass c : model.getAllObjectFactories()) {
//			contextPath.append(c._package().name());
//			contextPath.append(":");
//		}
//		contextPath.deleteCharAt(contextPath.length() - 1);
//
//		// Create new classloader for your directory
//		URLClassLoader cl = URLClassLoader.newInstance(new URL[] { dir.toURI()
//				.toURL() });
//
//		System.out.println("Context Path:" + contextPath);
//		System.out.println("Class URLs\n" + Arrays.toString(urlList.toArray()));
//		try {
//			System.out.println("Object Factory... "
//					+ cl.loadClass("fi.tut.wsdl.someservice.ObjectFactory"));
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// Make class list for JAXBContext
//		List<Class> classList = new ArrayList<Class>();
//		for (Mapping m : model.getMappings()) {
//			try {
//				classList.add(cl.loadClass(m.getType().getTypeClass()
//						.binaryName()));
//				System.out.println("Class: "
//						+ m.getType().getTypeClass().binaryName());
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//
//		// JAXBContext jaxbContext =
//		JAXBContext.newInstance(classList.toArray(new Class[] {}));
//		JAXBContext jaxbContext = JAXBContext.newInstance(
//				contextPath.toString(), cl);
//		System.out.println("SCHEMA::  ");
//
//		// Check schema regeneration
//		jaxbContext.generateSchema(new SchemaOutputResolver() {
//
//			@Override
//			public Result createOutput(String uri, String suggestedFileName)
//					throws IOException {
//				// TODO Auto-generated method stub
//				StreamResult result = new StreamResult(System.out);
//				result.setSystemId(systemId);
//				return result;
//			}
//
//		});
//		
//		
//		// Get classname from model
//		
//		QName element = new QName("fi.tut.fast.wsdl","WhatEver");
//		
//		 String className = model.get(element).getType().getTypeClass().binaryName();
//		 Object o = cl.loadClass(className).newInstance();
//		
//		 Method setName = o.getClass().getMethod("setName",String.class);
//		 
//		 setName.invoke(o, "newName");
//		
//
////		return new TypeHandler(systemId, model, jaxbContext, cl);
//
//	}

	// public class TypeHandler{
	//
	// private String systemId;
	// // private S2JJAXBModel model;
	// private JAXBContext context;
	// private Marshaller marshaller;
	// private Unmarshaller unmarshaller;
	// private ClassLoader loader;
	// private JAXBIntrospector introspector;
	// //
	// // public TypeHandler(String systemId, S2JJAXBModel model, JAXBContext
	// context, ClassLoader loader) throws JAXBException{
	// // this.systemId = systemId;
	// // this.model = model;
	// // this.context = context;
	// // this.loader = loader;
	// //
	// // this.introspector = context.createJAXBIntrospector();
	// // this.unmarshaller = context.createUnmarshaller();
	// // this.marshaller = context.createMarshaller();
	// //
	// // }
	//
	// public Object unmarshal(Node node) throws JAXBException{
	// return unmarshaller.unmarshal(node);
	// }
	//
	// public void marshal(Object obj, Node node) throws JAXBException{
	// System.out.println("Unmarshalling: " + introspector.getElementName(obj));
	// marshaller.marshal(obj, node);
	// }
	//
	// public Object newObject(QName element) throws InstantiationException,
	// IllegalAccessException, ClassNotFoundException{
	// // String name =
	// // model.get(element).getType().getTypeClass().binaryName();
	// // Object o = loader.loadClass(name).newInstance();
	// // return o;
	// return null;
	// }
	//
	// }
	//
	//

	public class TypeHandler {

		private String systemId;
		private SchemaTypeLoader stl;

		public TypeHandler(String systemId, SchemaTypeLoader stl) {
			this.systemId = systemId;
			this.stl = stl;
		}

		public XmlObject getElementTemplate(QName name) {
			if(name == DPWSConstants.EMPTY_MESSAGE_QNAME){
				return XmlObject.Factory.newInstance();
			}
			XmlObject obj = buildElement(name, stl);
			return obj;
		}

		public String getSystemId(){
			return systemId;
		}
		
		public XmlObject getEmptyElement(QName name) {
			
			if(name == DPWSConstants.EMPTY_MESSAGE_QNAME){
				return XmlObject.Factory.newInstance();
			}
			
			SchemaGlobalElement el = stl.findElement(name);
			XmlObject obj = stl.newInstance(el.getType(), new XmlOptions()
					.setSavePrettyPrint().setSavePrettyPrintIndent(4));
			XmlCursor c = obj.newCursor();
			c.toNextToken();
			c.beginElement(name);
			return obj;
		}

		public XmlObject getSimpleElement(QName name, String value) {
			SchemaGlobalElement el = stl.findElement(name);
			XmlObject obj = stl.newInstance(el.getType(), new XmlOptions()
					.setSavePrettyPrint().setSavePrettyPrintIndent(4));
			XmlCursor c = obj.newCursor();
			c.toNextToken();
			c.insertElementWithText(name, value);
			return obj;
		}

		public XmlObject populateElement(QName name, Map<String, String> params) {
			return buildAndPopulateElement(name, stl, params);
		}

		public XmlObject populateElement(QName name, String value) {
			return buildAndPopulateElement(name, stl, value);
		}

		public InputStream marshal(XmlObject obj) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			writeXml(obj, baos);
			return new ByteArrayInputStream(baos.toByteArray());
		}

		public boolean validate(XmlObject obj) {
			boolean isvalid = obj.validate(new XmlOptions()
					.setErrorListener(errs));
			for (XmlError err : errs) {
				logger.log(Level.SEVERE, err.getMessage());
			}
			return isvalid;
		}

		public XmlObject unmarshal(Node node, QName name) throws XmlException {
			SchemaGlobalElement el = stl.findElement(name);
			return stl.parse(node, el.getType(), new XmlOptions());
		}

		public void addChildElement(XmlObject obj, QName name, String value) {
			XmlCursor c = obj.newCursor();
			c.toLastChild();
		}
		
		public void getElementType(QName name){
			
			
		}
	}
	
	
	

//	public class TypeHandlerOld {
//
//		private String systemId;
//		private SchemaTypeSystem sys;
//
//		public TypeHandlerOld(String systemId, SchemaTypeSystem sys)
//				throws JAXBException {
//			this.systemId = systemId;
//			this.sys = sys;
//		}
//
//		public Object unmarshal(Node node) throws JAXBException {
//			return unmarshaller.unmarshal(node);
//		}
//
//		public void marshal(Object obj, Node node) throws JAXBException {
//			// System.out.println("Unmarshalling: " +
//			// introspector.getElementName(obj));
//			// marshaller.marshal(obj, node);
//		}
//
//		public Object newObject(QName element) throws InstantiationException,
//				IllegalAccessException, ClassNotFoundException {
//			// String name =
//			// model.get(element).getType().getTypeClass().binaryName();
//			// Object o = loader.loadClass(name).newInstance();
//			// return o;
//			return null;
//		}
//
//	}

	// public void loadGrammars(String systemId, List<TTypes> types){
	//
	// XMLSchemaLoader loader = new XMLSchemaLoader();
	//
	// //
	// // for (TTypes type : types) {
	// // for (Object o : type.getAny()) {
	// // if (o instanceof Element) {
	// // compiler.parseSchema(systemId, (Element) o);
	// // }
	// // }
	// // }
	//
	//
	// DOMInputSource dis = new
	// DOMInputSource((Element)types.get(0).getAny().get(0),systemId);
	//
	//
	// Grammar g = loader.loadGrammar(dis);
	// XSModel m = ((XSGrammar)g).toXSModel();
	// XSElementDeclaration
	//
	// // XMLInputSource xis = new XMLInputSource(systemId, systemId, systemId,
	// , "UTF-8")
	// .
	//
	//
	// // loader.loadInputList(arg0)
	// }

	public void writeXml(XmlObject obj, OutputStream out) {
		writeXml(obj.getDomNode(), System.out);
	}

	public void writeXml(Node node, OutputStream out) {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
			// "yes");
			transformer.transform(new DOMSource(node), new StreamResult(out));
			out.write('\n');
			out.flush();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeXml(XmlObject obj) {
		writeXml(obj, System.out);
	}

	public void writeXml(Node node) {
		writeXml(node, System.out);
	}
	
	public InputStream asInputStream(XmlObject obj){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeXml(obj, baos);
		return new ByteArrayInputStream(baos.toByteArray());
	}

	private String getBuiltInDefaultText(SchemaType t) {

		if (t.isNumeric()) {
			return "0";
		}

		switch (t.getBuiltinTypeCode()) {

		case SchemaType.BTC_ANY_SIMPLE:
		case SchemaType.BTC_ANY_TYPE:
		case SchemaType.BTC_ENTITIES:
		case SchemaType.BTC_ENTITY:
		case SchemaType.BTC_ID:
		case SchemaType.BTC_IDREF:
		case SchemaType.BTC_IDREFS:
		case SchemaType.BTC_NAME:
		case SchemaType.BTC_NCNAME:
		case SchemaType.BTC_NMTOKEN:
		case SchemaType.BTC_NMTOKENS:
		case SchemaType.BTC_NORMALIZED_STRING:
		case SchemaType.BTC_STRING:
		case SchemaType.BTC_TOKEN:
			return "ab";
		case SchemaType.BTC_LANGUAGE:
			return DPWSConstants.DEFAULT_LOCALE;
		case SchemaType.BTC_ANY_URI:
			return "http://*";
		case SchemaType.BTC_BASE_64_BINARY:
			return "00";
		case SchemaType.BTC_BOOLEAN:
			return "false";
		case SchemaType.BTC_DATE:
			return DatatypeConverter.printDate(Calendar.getInstance());
		case SchemaType.BTC_DATE_TIME:
			return DatatypeConverter.printDateTime(Calendar.getInstance());
		case SchemaType.BTC_DURATION:
			return "PT130S";
		case SchemaType.BTC_G_DAY:
			return "---01";
		case SchemaType.BTC_G_MONTH:
			return "--05";
		case SchemaType.BTC_G_MONTH_DAY:
			return "--05-01";
		case SchemaType.BTC_G_YEAR:
			return ((new SimpleDateFormat("yyyy")).format(Calendar
					.getInstance().getTime()));
		case SchemaType.BTC_G_YEAR_MONTH:
			return ((new SimpleDateFormat("yyyy-MM")).format(Calendar
					.getInstance().getTime()));
		case SchemaType.BTC_HEX_BINARY:
			return "c6d782";
		case SchemaType.BTC_QNAME:
			return "xsd:QName";
		case SchemaType.BTC_TIME:
			return DatatypeConverter.printTime(Calendar.getInstance());
		default:
			return "";
		}

	}
}
