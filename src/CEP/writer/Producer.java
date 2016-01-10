package CEP.writer;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;


/**
 * Created by ruveni on 06/11/15.
 */
public class Producer {
    private ConnectionFactory factory = null;
    private Connection connection = null;
    private Session session = null;
    private Destination destination = null;
    private MessageProducer producer = null;
    String inputString=null;

    public Producer(StringBuilder message) {
        inputString=message.toString();
    }

    public void sendMessage() {

        try {
            factory = new ActiveMQConnectionFactory(
                    ActiveMQConnection.DEFAULT_BROKER_URL);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue("ReflectQueue");
            producer = session.createProducer(destination);

            TextMessage message = session.createTextMessage();
            message.setText(inputString);
//            ObjectMessage message=session.createObjectMessage();
//            message.setObject(matrix);
            producer.send(message);
            System.out.println("Sent Message");

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
