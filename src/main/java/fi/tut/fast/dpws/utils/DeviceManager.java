package fi.tut.fast.dpws.utils;

import java.util.ArrayList;
import java.util.List;

import fi.tut.fast.dpws.device.Device;

public class DeviceManager {

	static List<Device> devices = new ArrayList<Device>();
	
	public static Device createDevice(String fName){
		return new Device(fName);
	}
	
	
	
	
	
}
