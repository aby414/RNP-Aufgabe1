import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MailFile{

    private static final Logger LOG = Logger.getLogger(MailFile.class.getName());

    private SSLSocketFactory sslSocketFactory;
    private SSLSocket sslSocket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;



    private String receiverEmail;
    private String attachment;


    //https://www.programcreek.com/java-api-examples/javax.net.ssl.SSLSocketFactory
    public MailFile(String host, Integer port) throws IOException {
        configLogger();
        sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try{
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(host,port);
            outputStream = new DataOutputStream(sslSocket.getOutputStream());
            inputStream = new DataInputStream(sslSocket.getInputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        



    }

    public static void main(String[] args){

    }


    public void handshake(){

    }

    private static void configLogger() {
        try {
            Handler handler = new FileHandler("log.txt");

            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);

            LOG.addHandler(handler);

            LOG.info("HALLO0");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
