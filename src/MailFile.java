import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MailFile {

	private static final String ENCODE_TYPE = "base64";
	private static final String MIME_VERSION = "1.0"; 
	private static final String CONFIGPATH = "config.properties";
	private static final Logger LOG = Logger.getLogger(MailFile.class.getName());

	private SSLSocketFactory sslSocketFactory;
	private static SSLSocket sslSocket;
	private static DataOutputStream outputStream;
	private static BufferedReader inputReader;
	private static Properties prop = new Properties();
	public static InputStream propInput;


	
	
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
		mf.authentication();
		mf.sendMail(prop.getProperty("mailadress"),args[0], prop.getProperty("subject"), prop.getProperty("body"), args[1]);
		
		try {
			mf.sendAndLog("QUIT");
			inputReader.close();
			outputStream.close();
			sslSocket.close();
			propInput.close();
		} catch (IOException e) {
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

	public void sendMail(String from, String to, String subject, String data, String filepath) {
		
		//https://stackoverflow.com/questions/858980/file-to-byte-in-java
		File file = new File(filepath);
		Path path = Paths.get(filepath);
		byte[] fileInBytes = null;
		try {
			fileInBytes = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sendAndLog("MAIL FROM:<" + from + ">");
		sendAndLog("RCPT TO:<" + to + ">");
		sendAndLog("DATA");

		send("From:<" + from + ">");
		send("To:<" + to + ">");
		send("Subject: " + subject);
		send("MIME-Version: " + MIME_VERSION);
		send("Content-Type: multipart/mixed; boundary=frontier");
	
		send("--frontier");
		
		//send("Content-Transfer-Encoding: quoted-printable\n");
		send("Content-Type: text/plain\n");
        send(data);
        send("--frontier");
        
        //https://wiki.selfhtml.org/wiki/MIME-Type/%C3%9Cbersicht
        send("Content-Transfer-Encoding: " + ENCODE_TYPE);
        send("Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document; name=" + file.getName());
		send("Content-Disposition: attachment; filename=" + file.getName()+"\n");
		send(Base64.getEncoder().encodeToString(fileInBytes));
		send("--frontier--");
		
	
        sendAndLog(".");
		
	}

	public void authentication() {
		Console console = System.console();
		char[] consolePassword = console.readPassword("Enter password: ");  
		String password = new String(consolePassword);
		String plainParam = encode("\0" + prop.getProperty("username") + "\0" + password);
		sendAndLog("AUTH PLAIN " + plainParam);
	}
	
	public String encode(String string) {
		return Base64.getEncoder().encodeToString(string.getBytes());
	}

	public void send(String message) {
		try {
			LOG.info("Client -> " + message);
			outputStream.writeBytes(message + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendAndLog(String message) {
		try {
			LOG.info("Client -> " + message);
			outputStream.writeBytes(message + "\n");
			logInput();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
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
