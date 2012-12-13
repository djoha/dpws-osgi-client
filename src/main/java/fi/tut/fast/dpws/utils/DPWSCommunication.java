/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.tut.fast.dpws.utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import fi.tut.fast.dpws.soap.HttpSOAPConnection;

/**
 *
 * @author Johannes
 */
public class DPWSCommunication {
    
    
    private static SOAPConnectionFactory connFactory;
    
    public static SOAPMessage sendMessage(SOAPMessage msg, String serviceAddress) throws IOException, SOAPException{
   
    	
        if(connFactory == null){
            connFactory = SOAPConnectionFactory.newInstance();
        }
//        
        
        SOAPConnection conn = new HttpSOAPConnection();
        
//        SOAPConnection conn = connFactory.createConnection();
        SOAPMessage out = conn.call(msg, serviceAddress);  
        conn.close();
        return out;
//        
        
//        URL siteUrl = new URL(serviceAddress);
//        HttpURLConnection conn = (HttpURLConnection) siteUrl.openConnection();
//        conn.setRequestMethod(HTTPConstants.HTTP_METHOD_POST);
//        conn.setDoOutput(true);
//        conn.setDoInput(true);
//        OutputStream out = conn.getOutputStream();
//        msg.writeTo(out);
//        out.flush();
//        out.close();
 
        // Get output message...
//        try{
//            return DPWSMessageFactory.recieveMessage(conn.getInputStream());
//        }catch(IOException ex) { 
//            System.err.println("Device returned Response Code " + conn.getResponseMessage());
//            return DPWSMessageFactory.recieveMessage(conn.getErrorStream());
//        }
    }
    
    public static SOAPConnection getNewSoapConnection() throws SOAPException{
//        if(connFactory == null){
//            connFactory = SOAPConnectionFactory.newInstance();
//        }
//        return connFactory.createConnection();
        
    	return new HttpSOAPConnection();
    }
    
    public static boolean checkNetworkInterface(String name){
        try {
            NetworkInterface iface = NetworkInterface.getByName(name);
            if (iface != null)
                    return checkIPv4address(iface);

            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while( ifaces.hasMoreElements() )
            {
                    iface = ifaces.nextElement();
                    if(iface.getDisplayName().equals(name)){
                            return checkIPv4address(iface);
                    }
            }

        } catch (SocketException ex) {
            Logger.getLogger(DPWSCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
        
   }
      
   private static boolean checkIPv4address(NetworkInterface iface){
      Enumeration<InetAddress> addresses = iface.getInetAddresses();
          while( addresses.hasMoreElements() )
          {
            InetAddress addr = addresses.nextElement();
            if( addr instanceof Inet4Address && !addr.isLoopbackAddress() )
            {
              return true;
            }
          }
          return false;
    }
   
//    public static InetAddress getInetAddress(String interfaceName) throws SocketException{
//        InetAddress addr = getInetAddressInternal(interfaceName);
//        
//        InetAddress addr = new InetAddress();
//        
//    }
    
    public static InetAddress getInetAddress(String interfaceName) throws SocketException{
                NetworkInterface iface = NetworkInterface.getByName(interfaceName);
        if (iface != null){
        Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while( addresses.hasMoreElements() )
            {
                InetAddress addr = addresses.nextElement();
                if( addr instanceof Inet4Address && !addr.isLoopbackAddress() )
                {
                    return addr;
                }
            }
        }
        return null;
    }
}
