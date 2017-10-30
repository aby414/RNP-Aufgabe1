import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MailFile{

    private static final Logger LOG = Logger.getLogger(MailFile.class.getName());
    public static final int TIMEOUT = 20;

    private SSLSocketFactory sslSocketFactory;
    private static SSLSocket sslSocket;
    private static DataOutputStream outputStream;
    private static BufferedReader inputReader;



    private String receiverEmail;
    private String attachment;


    //https://www.programcreek.com/java-api-examples/javax.net.ssl.SSLSocketFactory
    public MailFile(String host, Integer port) throws IOException {
        configLogger();
        sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try{
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(host,port);
            outputStream = new DataOutputStream(sslSocket.getOutputStream());
            inputReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            sslSocket.setKeepAlive(true);
            logInput();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args){
    	MailFile mf = null;
        try {
           mf = new MailFile("mailgate.informatik.haw-hamburg.de", 465);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        mf.handshake();
        try {
			inputReader.close();
			outputStream.close();
	        sslSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    public void authentication() {
    	
    }

    //handshake
    public void handshake(){
    	try {
    		LOG.info("Client: EHLO mailgate.informatik.haw-hamburg.de");
			outputStream.writeBytes("EHLO mailgate.informatik.haw-hamburg.de "+ '\r' + '\n');
			sleep();
			logInput();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void logInput() {
    	try {
			LOG.info("Server " + inputReader.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

    private void sleep() {
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
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
