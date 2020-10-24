package scc.srv;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import scc.rest.EntityResource;
import scc.utils.InsecureHostnameVerifier;




public class Server {


	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static final int PORT = 8080;

	public static void main(String[] args) throws UnknownHostException {
		InetAddress localHost = InetAddress.getLocalHost();
		String ip = localHost.getHostAddress();

		URI serverURI = URI.create(String.format("http://%s:%s/rest", ip, PORT));
		
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
				
		
		ResourceConfig config = new ResourceConfig();
		config.register(new EntityResource());


		try {
			JdkHttpServerFactory.createHttpServer( serverURI, config, SSLContext.getDefault());
			System.out.println(serverURI);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Invalid SSLL/TLS configuration.");
			e.printStackTrace();
			System.exit(1);
		}
	}

}