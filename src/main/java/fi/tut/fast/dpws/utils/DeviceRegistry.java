package fi.tut.fast.dpws.utils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlsoap.schemas.discovery.ByeType;
import org.xmlsoap.schemas.discovery.HelloType;
import org.xmlsoap.schemas.discovery.ProbeMatchType;
import org.xmlsoap.schemas.discovery.ProbeMatchesType;

import fi.tut.fast.dpws.device.remote.DeviceRef;

public class DeviceRegistry {


	private static final transient Logger logger = Logger
			.getLogger(DeviceRegistry.class.getName());
	
	Map<URI,DeviceRef> knownDevices;
	
	public DeviceRegistry(){
		knownDevices = new HashMap<URI,DeviceRef>();
	}
	
	// Adding DeviceRef
	public void registerDevice(HelloType hello){
		URI id = hello.getEndpointReference().getAddress().getValue();
		registerDeviceInternal(id, new DeviceRef(hello));
	}
	
	public void registerDevice(ProbeMatchesType matches){
		for(ProbeMatchType pm :  matches.getProbeMatch()){
			URI id = pm.getEndpointReference().getAddress().getValue();
			registerDeviceInternal(id, new DeviceRef(pm));
		}
	}
	
	private void registerDeviceInternal(URI id, DeviceRef dev){
		
		if(knownDevices.containsKey(id)){
			logger.info("Ignoring device " + id.toString());
			return;
		}
		knownDevices.put(id, dev);

		logger.info("Device Discovered:" + dev.toString());

		// Test Operation Invokation
		
//		OperationReference op1 = dev.getService("SomeService").getOperation(
//				"OperationOne");
//
//		Map<String, String> params = new HashMap<String, String>();
//
//		params.put("K1", "4.2");
//		params.put("K2", "6.3");
//		params.put("@lang", "FR");
//
//		XmlObject iobj = op1.getInputParamter(params);
//
//		System.out.println("Invoking OperationOne:");
//		try {
//			DPWSXmlUtil.getInstance().writeXml(iobj);
//			XmlObject oobj = op1.invoke(iobj);
//			System.out.println("Response:");
//			DPWSXmlUtil.getInstance().writeXml(oobj);
//		} catch (Exception ex) {
//			logger.log(Level.SEVERE, "Error invoking OperationOne", ex);
//		}

	}
	
	public DeviceRef getDevice(URI id){
		return knownDevices.get(id);
	}
	
	public DeviceRef removeDevice(URI id){
		return knownDevices.remove(id);
	}
	
	public void reportBye(URI id){
		DeviceRef dev = knownDevices.get(id);
		if(dev != null){
			dev.reportBye();
		}
	}
	
	public void reportBye(ByeType bye){
		 reportBye(bye.getEndpointReference().getAddress().getValue());
		 logger.info("Bye Message Recieved from " + bye.getEndpointReference().getAddress().getValue());
	}
}
