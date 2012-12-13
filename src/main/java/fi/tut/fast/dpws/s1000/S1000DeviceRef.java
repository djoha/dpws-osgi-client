package fi.tut.fast.dpws.s1000;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import fi.tut.fast.dpws.device.remote.DeviceRef;

public class S1000DeviceRef extends DeviceRef {

	public static final String S1000_CONFIG_SYSTEM_PATH = "/config/system/html";

	public void restart() {
		try {
			URL siteUrl = new URL(String.format("http://%s%s", getXAddress(),
					S1000_CONFIG_SYSTEM_PATH));
			HttpURLConnection conn = (HttpURLConnection) siteUrl
					.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes("reboot=reboot&__end=");
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
//				System.out.println(line);
			}
			in.close();
			conn.disconnect();
		} catch (Exception ex) {
			System.err.println("Restart Attempt Failed.");
			ex.printStackTrace();
		}
	}
	
	public void browse(){

        if( !java.awt.Desktop.isDesktopSupported() ) {
            return;
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
            return;
        }

        try {
            java.net.URI uri = new java.net.URI( "http://" + getXAddress());
            desktop.browse( uri );
        } catch ( IOException e ) {
//           logger.log.error("Browse Failed.");
//           Log.printStackTrace(e);
        } catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
