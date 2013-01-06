package fi.tut.fast.dpws.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.jetty.client.Address;
import org.xmlsoap.schemas.discovery.ProbeMatchesType;
import org.xmlsoap.schemas.discovery.ProbeType;

import fi.tut.fast.MulticastConstants;
import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.DpwsClient;
import fi.tut.fast.dpws.device.Device;
import fi.tut.fast.dpws.test.TestDevice;

public class DeviceManager {

	private static final transient Logger logger = Logger
			.getLogger(DeviceManager.class.getName());

	public static final int UNICAST_UDP_REPEAT = DPWSConstants.UNICAST_UDP_REPEAT;
	public static final int UDP_MIN_DELAY = DPWSConstants.UDP_MIN_DELAY;
	public static final int UDP_MAX_DELAY = DPWSConstants.UDP_MAX_DELAY;
	public static final int UDP_UPPER_DELAY = DPWSConstants.UDP_UPPER_DELAY;

	private int port = 0;
	private String networkInterface;
	private String host;
	private NetworkInterface iface;
	private CamelContext camelContext;

	static Map<String,Device> devices = new HashMap<String,Device>();

	@Produce(uri = "direct:probeMatcher")
	Responder p;

	DatagramSocket s;

	interface Responder {
		public void sendProbeMatch(byte[] msg);
	}

//	public static Device createDevice(String fName) {
//		return new Device(fName);
//	}

	public void init() throws Exception {
		s = new DatagramSocket(port,getAddress());
	}

	public void destroy() {

	}

	public void probeReceived(Exchange ex) throws JAXBException,
			SOAPException, IOException {

//		System.out.format("Headers: \n   SourceAddress: %s:%s\n  PacketLength %s\n",
//				ex.getIn().getHeader(MulticastConstants.SOURCE_ADDRESS),
//				ex.getIn().getHeader(MulticastConstants.SOURCE_PORT),
//				ex.getIn().getHeader(MulticastConstants.PACKET_LENGTH));
		
//		SOAPMessage msg = DPWSMessageFactory.recieveMessage(ex.getIn().getBody(InputStream.class));
//		msg.writeTo(System.out);
		
		System.out.println("Device Manager: Probe Received.");
		ProbeType probe = (ProbeType) DPWSXmlUtil.getInstance().unmarshalSoapBody(ex);
		
		try {
			URI address = new URI(ex.getIn().getHeader(MulticastConstants.SOURCE_ADDRESS,String.class));
			transmitMatches(probe,
					InetAddress.getByName(address.toString()),
					Integer.parseInt(ex.getIn().getHeader(MulticastConstants.SOURCE_PORT,String.class)));

		} catch (URISyntaxException e) {
			logger.log(Level.SEVERE, "Can't Parse Return Address", e);
			return;
		}
		
		
		

	}

	public class ProbeScheduleEntry implements Comparable<ProbeScheduleEntry> {

		int delay;
		ProbeMatchesType match;

		public ProbeScheduleEntry(ProbeMatchesType match, int delay) {
			this.match = match;
			this.delay = delay;
		}

		@Override
		public int compareTo(ProbeScheduleEntry in) {
			return delay - in.delay;
		}

		public int getDelay() {
			return delay;
		}

		public ProbeMatchesType getProbeMatches() {
			return match;
		}
	}

	public void transmitMatches(ProbeType probe, InetAddress dest, int destPort) throws JAXBException {

		SortedSet<ProbeScheduleEntry> transmitSchedule = new TreeSet<ProbeScheduleEntry>();

		Random random = new Random();

		for (Device d : devices.values()) {

			ProbeMatchesType matches = d.createProbeMatches(probe);
			if (matches != null) {
				int t_delay = random.nextInt(UDP_MAX_DELAY - UDP_MIN_DELAY)
						+ UDP_MIN_DELAY;
				for (int ii = UNICAST_UDP_REPEAT; ii > 0; ii--) {
					transmitSchedule.add(new ProbeScheduleEntry(matches,
							t_delay));
					t_delay = t_delay * 2;
					if (t_delay > UDP_UPPER_DELAY) {
						t_delay = UDP_UPPER_DELAY;
					}
				}
			}
		}
				
		(new TransmitThread(transmitSchedule,dest,destPort)).start();

	}

	class TransmitThread extends Thread {
		SortedSet<ProbeScheduleEntry> transmitSchedule;
		InetAddress dest;
		int destPort;

		TransmitThread(SortedSet<ProbeScheduleEntry> transmitSchedule, InetAddress dest, int destPort) {
			this.transmitSchedule = transmitSchedule;
			this.dest = dest;
			this.destPort = destPort;
		}

		public void run() {

			ProbeScheduleEntry n;
			ProbeScheduleEntry p;
			Iterator<ProbeScheduleEntry> list = transmitSchedule.iterator();
			int delay = 0;

			while (list.hasNext()) {
				n = list.next();
				if(n.getDelay() - delay > 5){
					try {
						sleep(n.getDelay() - delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				ByteArrayOutputStream probeMatch = new ByteArrayOutputStream();
				try {
					DPWSMessageFactory.getDiscoveryProbe().writeTo(probeMatch);
				} catch (SOAPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				byte[] buf = probeMatch.toByteArray();
				DatagramPacket out = new DatagramPacket(buf, buf.length,dest,destPort);
				try {
					s.send(out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				delay = n.getDelay();
			}

		}
	}

	private InetAddress getAddress() throws UnknownHostException,
			SocketException {
		if (host != null) {
			return InetAddress.getByName(host);
		}

		InetAddress addr = null;
		Class addrClass = Inet4Address.class;
		
		for (Enumeration<InetAddress> as = getInterface().getInetAddresses(); as
				.hasMoreElements();) {
			InetAddress a = as.nextElement();
			if (addrClass.isInstance(a)) {
				addr = a;
			}
		}

		if (addr == null) {
			addr = getInterface().getInetAddresses().nextElement();
		}

		return addr;

	}

	private NetworkInterface getInterface() throws SocketException,
			UnknownHostException {
		if (iface != null) {
			return iface;
		}

		if (getNetworkInterface() == null) {
			iface = NetworkInterface.getByInetAddress(getAddress());
			if (iface == null) {
				iface = NetworkInterface.getNetworkInterfaces().nextElement();
			}
		} else {
			iface = NetworkInterface.getByName(getNetworkInterface());
		}
		return iface;
	}

	public void addDevice(Device dev) throws UnknownHostException, SocketException, Exception {
		
		String id = newDeviceId();
		devices.put(id,dev);
		camelContext.addRoutes(new DeviceRouteBuilder(dev,id,getAddress().getHostName()));
		
	}
	
	class DeviceRouteBuilder extends RouteBuilder{

		String id;
		Device dev;
		String host;
		
		public DeviceRouteBuilder(Device dev,String id,String host){
			this.id = id;
			this.dev = dev;
			this.host = host;
		}
		
		@Override
		public void configure() throws Exception {			
			from("jetty://http://" + host + ":12367/device/" + id).bean(dev, Device.BEAN_METHOD_NAME);
		}
		
	}

	private int deviceNum = 0;
	private String newDeviceId(){
		return String.format("d%03d",deviceNum++);
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}

}
