package fi.tut.fast.dpws.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.commons.io.IOUtils;

import fi.tut.fast.dpws.DpwsClient;

public class DiscoveryManager {

	private static final transient Logger logger = Logger
			.getLogger(DpwsClient.class.getName());

	private static int SOCKET_TIMEOUT = 10000;

	private String networkInterface;
	private int sourcePort = 0;
	private int muticastPort = 3702;
	private NetworkInterface iface;
	private String multicastGroup = "239.255.255.250";
	private String host;
	private int maxPacketSize = 2048;

	private URI mcGroup;

	private Thread dlThread;

	private boolean done = false;
	private Queue<Runnable> messageQueue;

	MulticastSocket s;

	@Produce(uri = "direct:discoveryManager")
	Distributor sender;

	interface Distributor {
		public void send(byte[] packet);

		public void sendPacket(DatagramPacket p);

		public void send(Exchange ex);
	}

	public void init() throws IOException {

		InetAddress addr = getSourceAddress();

		messageQueue = new MessageQueue();
		
		logger.info(String.format(
				"Starting Discovery Socket on interface %s (%s) ",
				getInterface().getDisplayName(), addr.getHostAddress()));

		s = new MulticastSocket(new InetSocketAddress(addr.getHostAddress(),
				sourcePort));
		s.setReuseAddress(true);
		s.setNetworkInterface(getInterface());
		s.setLoopbackMode(true);
	}

	public void destroy(){
		done = true;
		s.close();
	}
	
	public void sendProbe(Exchange exchange) throws IOException {

		done = true;
		
		// Send Probe
		InputStream is = exchange.getIn().getBody(InputStream.class);

		byte[] buf = IOUtils.toByteArray(is);
		DatagramPacket out = new DatagramPacket(buf, buf.length,
				getMulticastGroup(), muticastPort);

		try {
			s.send(out);
		} catch (SocketException ex) {
			logger.log(Level.SEVERE,
					"Socket Closed before DatagramPacket could be sent.", ex);
		} catch (IOException ex) {
			logger.log(Level.SEVERE,
					"Socket Closed before DatagramPacket could be sent.", ex);
		}

		// Start listening for responses
		s.setSoTimeout(SOCKET_TIMEOUT);
		done = false;
		
		dlThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!done) {
					try {
						consume();
					} catch (SocketTimeoutException e) {
						logger.info("Discovery Timed Out.");
						done = true;
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		dlThread.setDaemon(true);
		dlThread.start();

	}

	private void consume() throws IOException, SocketTimeoutException {
		byte buf[] = new byte[maxPacketSize];
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		s.receive(pack);
		try {
			messageQueue.add(new ExchangeDeliveryJob(pack.getData(), pack
					.getLength()));
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"Exception Caught while processing UDP message.", e);
		}
	}

	class ExchangeDeliveryJob implements Runnable {

		byte buf[];
		int length;

		public ExchangeDeliveryJob(byte[] buf, int len) {
			this.buf = Arrays.copyOf(buf, len);
			this.length = len;
//			
//			System.out.println("Recieved packet...\n[[START OF PACKET]]");
//			try {
//				IOUtils.write(buf, System.out);
//				System.out.flush();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			System.out.println("\n[[END OF PACKET]]");
		}

		@Override
		public void run() {
			sender.send(buf);
		}

	}

	class MessageQueue extends ConcurrentLinkedQueue<Runnable> {

		Thread processor;
		private final Object lock = new Object();

		public MessageQueue() {
			processor = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							synchronized (lock) {
								lock.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						while (!isEmpty()) {
							poll().run();
						}
					}
				}
			});
			processor.start();
		}

		@Override
		public boolean offer(Runnable job) {
			boolean result = super.offer(job);
			synchronized (lock) {
				lock.notifyAll();
			}
			return result;
		}

	}

	public URI getMulticastAddress() throws URISyntaxException {
		if (mcGroup == null) {
			mcGroup = new URI(multicastGroup);
		}
		return mcGroup;
	}

	private InetAddress getSourceAddress() throws UnknownHostException,
			SocketException {
		if (host != null) {
			return InetAddress.getByName(host);
		}

		InetAddress addr = null;
		Class addrClass;
		if (getMulticastGroup() instanceof Inet4Address) {
			addrClass = Inet4Address.class;
		} else {
			addrClass = Inet6Address.class;
		}

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

	public InetAddress getMulticastGroup() throws UnknownHostException {
		return InetAddress.getByName(multicastGroup);
	}

	private NetworkInterface getInterface() throws SocketException,
			UnknownHostException {
		if (iface != null) {
			return iface;
		}

		if (getNetworkInterface() == null) {
			iface = NetworkInterface.getByInetAddress(getMulticastGroup());
			if (iface == null) {
				iface = NetworkInterface.getNetworkInterfaces().nextElement();
			}
		} else {
			iface = NetworkInterface.getByName(getNetworkInterface());
		}
		return iface;
	}

	public String getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}

	public int getMuticastPort() {
		return muticastPort;
	}

	public void setMuticastPort(int muticastPort) {
		this.muticastPort = muticastPort;
	}

	public NetworkInterface getIface() {
		return iface;
	}

	public void setIface(NetworkInterface iface) {
		this.iface = iface;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getMaxPacketSize() {
		return maxPacketSize;
	}

	public void setMaxPacketSize(int maxPacketSize) {
		this.maxPacketSize = maxPacketSize;
	}

	public void setMulticastGroup(String multicastGroup) {
		this.multicastGroup = multicastGroup;
	}

}
