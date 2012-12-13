package fi.tut.fast.dpws;

import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import fi.tut.fast.dpws.device.remote.DeviceRef;
import fi.tut.fast.dpws.device.remote.OperationReference;
import fi.tut.fast.dpws.device.remote.ServiceRef;

public interface IDpwsClient {

	public void dpwsScan();
	public Map<String,DeviceRef> getDiscoveredDevices();
	public Map<String,ServiceRef> listServices(String deviceId);
	public Map<String,OperationReference> listOperations(String serviceId);
	public XmlObject getInputXmlTemplate(String operationid);
	public XmlObject invokeOperation(String operationId, XmlObject input);
	
}
