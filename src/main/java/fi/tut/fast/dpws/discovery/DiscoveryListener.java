package fi.tut.fast.dpws.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.impl.DefaultExchange;

import fi.tut.fast.dpws.DPWSConstants;
import fi.tut.fast.dpws.utils.DPWSMessageFactory;

public class DiscoveryListener {


	
	private CamelContext camelContext;
	
	private MulticastSocket s;
	private boolean done = false;
	private Thread dlThread;
	private List<IHelloByeListener> listeners;
	private Queue<Runnable> messageQueue;
	
	@Produce(uri="direct:wsdListener")
	Distributor sender;
	
	interface Distributor{
		public void send(byte[] packet);
		public void sendPacket(DatagramPacket p);
		public void send(Exchange ex);
	}
	
	public DiscoveryListener(){

	}
	
	public void registerListener(IHelloByeListener listener){
		if(!listeners.contains(listener)){
			listeners.add(listener);
		}
	}
	
	public void init() throws IOException{

		DPWSMessageFactory.init();
		
		listeners = new ArrayList<IHelloByeListener>();
		messageQueue = new MessageQueue();
		
		s = new MulticastSocket(DPWSConstants.WS_DISOVERY_PORT);
		s.setReuseAddress(true);
		s.setNetworkInterface(java.net.NetworkInterface.getByName("en0"));
		s.joinGroup(InetAddress.getByName(DPWSConstants.WS_DISCOVERY_MULTICAST_GROUP));
		s.setSoTimeout(DPWSConstants.WS_DISCOVERY_SOCKET_TIMEOUT);

		dlThread = new Thread(new Runnable(){

			@Override
			public void run() {
				while(!done){
					try {
						consume();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				try {
					s.leaveGroup(InetAddress.getByName(DPWSConstants.WS_DISCOVERY_MULTICAST_GROUP));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				s.close();
				
			}
			
		});
		dlThread.setDaemon(true);
		dlThread.start();
	}
	
	private void consume() throws IOException{
		byte buf[] = new byte[DPWSConstants.WSD_PROBE_MAX_SIZE];
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		try{
			s.receive(pack);
//			System.out.println("Received data from: " + pack.getAddress().toString() +
//				    ":" + pack.getPort() + " with length: " +
//				    pack.getLength());
//			System.out.write(buf);
//			SOAPMessage msg;
			try {
//				msg = DPWSMessageFactory.recieveMessage(new ByteArrayInputStream(pack.getData(), 0, pack.getLength()));
//				msg.writeTo(System.out);
//				sender.send(new ByteArrayInputStream(pack.getData(), 0, pack.getLength()));
//				messageQueue.add(new DeliveryJob(pack.getData(),pack.getLength()));
				messageQueue.add(new ExchangeDeliveryJob(pack.getData(),pack.getLength()));
//				messageQueue.add(new PacketDeliveryJob(pack));
				
			} catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Exception Caught while processing UDP message.", e);
				System.out.println("Exception Caught while receiving UDP Multicast. Check logs...\n" + e.getMessage());
			}
			
			
		}catch(java.net.SocketTimeoutException ex){
			// Timed out.. Listen again.
		}
	}
	
	public void destroy(){
		done = true;
	}
	
	public CamelContext getCamelContext() {
		return camelContext;
	}

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}

	class MessageQueue extends ConcurrentLinkedQueue<Runnable>{
		
		Thread processor;
		private final Object lock = new Object();
		public MessageQueue(){
			processor = new Thread(new Runnable(){
				@Override
				public void run() {
					while(true){
						try {
							synchronized(lock){
								lock.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						while(!isEmpty()){
							poll().run();
						}
					}
				}
			});
			processor.start();
		}
		
		@Override
		public boolean offer(Runnable job){
			boolean result = super.offer(job);
			synchronized(lock){
				lock.notifyAll();
			}
			return result;
		}
		
	}
	
	class DeliveryJob implements Runnable{
		
		byte buf[];
		int length;
		
		public DeliveryJob(byte[] buf, int len){
			this.buf = Arrays.copyOf(buf,len);
			this.length = len;
		}

		@Override
		public void run() {
			sender.send(buf);
		}
		
	}	
	
	class PacketDeliveryJob implements Runnable{
		
		DatagramPacket packet;
		
		public PacketDeliveryJob(DatagramPacket p){
			this.packet = p;
		}

		@Override
		public void run() {
			sender.sendPacket(packet);
		}
		
	}
	
	class ExchangeDeliveryJob implements Runnable{
		
		byte buf[];
		int length;
		
		public ExchangeDeliveryJob(byte[] buf, int len){
			this.buf = Arrays.copyOf(buf,len);
			this.length = len;
		}

		@Override
		public void run() {
			Exchange ex = new DefaultExchange(camelContext);
			ex.getIn().setBody(buf);
			sender.send(buf);
		}
		
	}
	
}
