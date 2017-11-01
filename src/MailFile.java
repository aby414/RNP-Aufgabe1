import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.awt.SecondaryLoop;
import java.io.*;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MailFile {

	private static final String CONFIGPATH = "config.properties";
	private static final Logger LOG = Logger.getLogger(MailFile.class.getName());
	public static final int TIMEOUT = 20;

	private SSLSocketFactory sslSocketFactory;
	private static SSLSocket sslSocket;
	private static DataOutputStream outputStream;
	private static BufferedReader inputReader;
	private static Properties prop = new Properties();
	public static InputStream propInput;

	private Base64.Encoder encoder;

	private String receiverEmail;
	private String attachment;

	// https://www.programcreek.com/java-api-examples/javax.net.ssl.SSLSocketFactory
	public MailFile() {
		configProperties();
		configLogger();
		sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			sslSocket = (SSLSocket) sslSocketFactory.createSocket(prop.getProperty("hostname"), Integer.parseInt(prop.getProperty("portnumber")));
			outputStream = new DataOutputStream(sslSocket.getOutputStream());
			inputReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
			logInput();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		MailFile mf = new MailFile();

		mf.handshake();
		mf.authentication("sah");
		mf.sendMail(prop.getProperty("mailadress"),args[0], prop.getProperty("subject"), prop.getProperty("body"));
		try {
			mf.sendAndLog("QUIT");
			inputReader.close();
			outputStream.close();
			sslSocket.close();
			propInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void configProperties() {
			try {
				 propInput = new FileInputStream(CONFIGPATH);
				prop.load(propInput);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	}

	public void sendMail(String from, String to, String subject, String data) {
		sendAndLog("MAIL FROM:<" + from + ">");
		sendAndLog("RCPT TO:<" + to + ">");
		sendAndLog("DATA");

		send("From:<" + from + ">");
		send("To:<" + to + ">");
		send("Subject: " + subject);
		send(data);
		sendAndLog(".");
	}

	public void authentication(String password) {

		sendAndLog("AUTH LOGIN");

		sendAndLog("YWJ5NDE0");

		sendAndLog("Um95YWxzMTIz");
	}
	
	public String encode(String string) {
		return encoder.encodeToString(string.getBytes());
	}

	public void send(String message) {
		try {
			LOG.info("Client -> " + message);
			outputStream.writeBytes(message + "\r" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendAndLog(String message) {
		try {
			LOG.info("Client -> " + message);
			outputStream.writeBytes(message + "\r" + "\n");
			logInput();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// handshake
	public void handshake() {
		sendAndLog("EHLO mailgate.informatik.haw-hamburg.de ");
	}

	public void logInput() {
		String str;
		try {
			while ((str = inputReader.readLine()).charAt(3) == '-') {
				LOG.info("Server -> " + str);
			}
			LOG.info("Server -> " + str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void configLogger() {
		try {
			Handler handler = new FileHandler("log.txt");

			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);

			LOG.addHandler(handler);

			LOG.info("Client-Server Communication");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
