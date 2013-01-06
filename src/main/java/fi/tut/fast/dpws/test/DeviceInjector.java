package fi.tut.fast.dpws.test;

import java.net.SocketException;
import java.net.UnknownHostException;

import fi.tut.fast.dpws.utils.DeviceManager;

public class DeviceInjector {

	
	private DeviceManager manager;

	public DeviceManager getManager() {
		return manager;
	}

	public void setManager(DeviceManager manager) {
		this.manager = manager;
	}
	
	public void init() throws UnknownHostException, SocketException, Exception{
		manager.addDevice(new TestDevice());
		System.out.println("Creating new TestDevice");
	}
	
	
}
