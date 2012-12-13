package fi.tut.fast.dpws.device;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xmlsoap.schemas.devprof.DeviceMetadataDialectURIs;
import org.xmlsoap.schemas.devprof.DeviceRelationshipTypeURIs;
import org.xmlsoap.schemas.devprof.HostServiceType;
import org.xmlsoap.schemas.devprof.LocalizedStringType;
import org.xmlsoap.schemas.devprof.Relationship;
import org.xmlsoap.schemas.devprof.ThisDeviceType;
import org.xmlsoap.schemas.devprof.ThisModelType;
import org.xmlsoap.schemas.mex.Metadata;
import org.xmlsoap.schemas.mex.MetadataSection;

import fi.tut.fast.dpws.utils.DPWSXmlUtil;
import fi.tut.fast.dpws.utils.LocalizedTextMap;


public class Device {

	LocalizedTextMap manufacturers = new LocalizedTextMap();
	LocalizedTextMap friendlyNames = new LocalizedTextMap();
	LocalizedTextMap modelNames = new LocalizedTextMap();
	String serialNumber;
	String modelNumber;
	String firmwareVersion;
	URI manufacturerUrl; // = new URI("http://www.tut.fi/fast");
	URL endpointAddress;
	URI modelUrl;
	URI presentationUrl;
	
	static org.xmlsoap.schemas.devprof.ObjectFactory defProfFactory;
	static org.xmlsoap.schemas.mex.ObjectFactory mexObjFactory;
	protected Map<String,Service> hostedServices = new HashMap<String,Service>();
	
	public Device(){
		
	}
	
	public Device(String fName){
		friendlyNames.put(fName);
	}

	// Friendly Name
	public String getFriendlyName(){
		return friendlyNames.get();
	}
	
	public void setFriendlyName(String str){
		friendlyNames.put(str);
	}

	public void addFriendlyName(String locale, String name){
		friendlyNames.put(locale, name);
	}
	
	public String getFriendlyName(String locale){
		return friendlyNames.get(locale);
	}
	
	// Manufacturer
	public void setManufacturer(String str){
		manufacturers.put(str);
	}
	
	public String getManufacturer(){
		return manufacturers.get();
	}
	
	public void addManufacturer(String locale, String name){
		manufacturers.put(locale, name);
	}
	
	public String getManufacturer(String locale){
		return manufacturers.get(locale);
	}
	
	// Model Name
	public void setModelName(String str){
		modelNames.put(str);
	}
	
	public String getModelName(){
		return modelNames.get();
	}
	
	public void addModelName(String locale, String name){
		modelNames.put(locale, name);
	}
	
	public String getModelName(String locale){
		return modelNames.get(locale);
	}
	
	// FirmwareVersion
	public void setFirmwareVersion(String str){
		firmwareVersion = str;
	}
	
	public String getFirmwareVersion(){
		return firmwareVersion;
	}	

	// Serial Number
	public void setSerialNumber(String str){
		serialNumber = str;
	}
	
	public String getSerialNumber(){
		return serialNumber;
	}
	// Model Number
	public void setModelNumber(String str){
		modelNumber = str;
	}
	
	public String getModelNumber(){
		return modelNumber;
	}
	
	
	public URI getManufacturerUrl() {
		return manufacturerUrl;
	}

	public void setManufacturerUrl(URI manufacturerUrl) {
		this.manufacturerUrl = manufacturerUrl;
	}

	public URI getModelUrl() {
		return modelUrl;
	}

	public void setModelUrl(URI modelUrl) {
		this.modelUrl = modelUrl;
	}

	public URI getPresentationUrl() {
		return presentationUrl;
	}

	public void setPresentationUrl(URI presentationUrl) {
		this.presentationUrl = presentationUrl;
	}

	public Metadata getDeviceMetadata() throws JAXBException{
		
		// Object Factories
		if(defProfFactory == null){
			defProfFactory = new org.xmlsoap.schemas.devprof.ObjectFactory();
		}
		if(mexObjFactory == null){
			mexObjFactory = new org.xmlsoap.schemas.mex.ObjectFactory();
		}
    	
		
	// ThisModel
    	ThisModelType thisModel = new ThisModelType();
    	if(modelNumber != null){
        	thisModel.setModelNumber(modelNumber);
    	}
    	if(manufacturerUrl != null){
        	thisModel.setManufacturerUrl(manufacturerUrl);
    	}
    	if(modelUrl != null){
        	thisModel.setModelUrl(modelUrl);
    	}
    	if(presentationUrl != null){
        	thisModel.setPresentationUrl(presentationUrl);
    	}
    	
    	for(LocalizedStringType m : manufacturers.toList()){
    		thisModel.addManufacturer(m);
    	}
	
		for(LocalizedStringType m : modelNames.toList()){
			thisModel.addModelName(m);
		}
		
   // ThisDevice
    	ThisDeviceType thisDevice = new ThisDeviceType();
    	if(firmwareVersion != null){
    		thisDevice.setFirmwareVersion(firmwareVersion);
    	}
    	if(serialNumber != null){
    		thisDevice.setSerialNumber(serialNumber);
    	}
    	for(LocalizedStringType m : friendlyNames.toList()){
    		thisDevice.addFriendlyName(m);
    	}

    	MetadataSection modelSection = mexObjFactory.createMetadataSection();
    	modelSection.setDialect(DeviceMetadataDialectURIs.ThisModel.value());
    	modelSection.setAny(defProfFactory.createThisModel(thisModel));
    	
    	MetadataSection deviceSection = mexObjFactory.createMetadataSection();
    	deviceSection.setDialect(DeviceMetadataDialectURIs.ThisDevice.value());
    	deviceSection.setAny(defProfFactory.createThisDevice(thisDevice));
    	

    	Metadata metadata = mexObjFactory.createMetadata();
    	metadata.addMetadataSection(deviceSection);
    	metadata.addMetadataSection(modelSection);

    	if(!hostedServices.isEmpty()){
        	MetadataSection relationshipSection = mexObjFactory.createMetadataSection();
        	relationshipSection.setDialect(DeviceMetadataDialectURIs.Relationship.value());
        	Relationship rel = new Relationship(DeviceRelationshipTypeURIs.host.value());
        	relationshipSection.setAny(rel);

        	for(Service s : hostedServices.values()){
        		rel.addAny(s.getHostedMetadata());
        	}
        	metadata.addMetadataSection(relationshipSection);
    	}   	
    	return metadata;
	}
	
	
}