package fi.tut.fast.dpws.discovery;

import javax.xml.soap.SOAPMessage;

public interface IHelloByeListener {

	public void helloReceived(SOAPMessage message);
	public void byeReceived(SOAPMessage message);

}
