package CEP.reader;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.BasicConfigurator;

import java.util.*;

/**
 * Created by ruveni on 30/10/15.
 */
public class Consumer extends TimerTask {
    double maxRow=0,maxColumn=0,minRow=240,minCol=240,rowVal=0,colVal=0;
    // URL of the JMS server
    private static String url = "tcp://localhost:61616";
    // Name of the queue we will receive messages from
    private static String subject = "BoundaryQuery";
    MessageConsumer consumer = null;
    Connection connection = null;
    Session session = null;
    Timer timer=null;
    String[] msgArray=null,colArray=null,rowArray=null;

    public Consumer(Timer timer) {
        this.timer=timer;

        try {
            BasicConfigurator.configure();
            // Getting JMS connection from the server
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection();
            connection.start();
            // Creating session for sending messages
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            // Getting the queue
            Destination destination = session.createQueue(subject);
            // MessageConsumer is used for receiving (consuming) messages

            consumer = session.createConsumer(destination);
            // Here we receive the message.

            // By default this call is blocking, which means it will wait

            // for a message to arrive on the queue.
        } catch (Exception e) {
            e.printStackTrace();
            timer.cancel();
        }
    }

    @Override
    public void run() {
        try {
            Message message = consumer.receive();
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String msg = textMessage.getText();
                System.out.println("Received message " + msg);
                System.out.println();
                //decodes the message
                String[] msgArray=msg.split(",");
                String[] filePathArray=msgArray[0].split(":");
                String[] boundaryArray=msgArray[1].split(":");
                //gets the file path and the boundary
                System.out.println(filePathArray[1]+" "+boundaryArray[1]);
            }
        } catch (Exception e) {
            timer.cancel();
            e.printStackTrace();
            try{
                connection.close();
            }catch (Exception ex){
                //ex.printStackTrace();
            }
        }
    }
}