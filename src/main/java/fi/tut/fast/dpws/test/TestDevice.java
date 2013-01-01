package fi.tut.fast.dpws.test;

import java.net.URI;
import java.net.URISyntaxException;

import fi.tut.fast.dpws.device.Device;

public class TestDevice extends Device {

	
	public TestDevice() {
		super("TestDevice");
		setManufacturer("FASTLab");
		setModelName("D100000");
		setModelNumber("1-001");
		setSerialNumber("001-001");
		setFirmwareVersion("0.0.1");
		try {
			setManufacturerUrl(new URI("http://www.tut.fi/fast"));
		} catch (URISyntaxException e) {
			//won't happen.
		}
		
	}

	
	
}
